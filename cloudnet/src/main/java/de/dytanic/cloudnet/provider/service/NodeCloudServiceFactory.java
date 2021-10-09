/*
 * Copyright 2019-2021 CloudNetService team & contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dytanic.cloudnet.provider.service;

import aerogel.Inject;
import aerogel.Singleton;
import aerogel.auto.Provides;
import com.google.common.collect.ComparisonChain;
import de.dytanic.cloudnet.cluster.IClusterNodeServer;
import de.dytanic.cloudnet.cluster.IClusterNodeServerProvider;
import de.dytanic.cloudnet.driver.channel.ChannelMessage;
import de.dytanic.cloudnet.driver.channel.ChannelMessageTarget.Type;
import de.dytanic.cloudnet.driver.network.buffer.DataBufFactory;
import de.dytanic.cloudnet.driver.provider.service.CloudServiceFactory;
import de.dytanic.cloudnet.driver.provider.service.DefaultCloudServiceFactory;
import de.dytanic.cloudnet.driver.service.ServiceConfiguration;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.dytanic.cloudnet.service.ICloudServiceManager;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
@Provides(CloudServiceFactory.class)
public class NodeCloudServiceFactory extends DefaultCloudServiceFactory implements CloudServiceFactory {

  private final ICloudServiceManager serviceManager;
  private final IClusterNodeServerProvider nodeServerProvider;

  @Inject
  public NodeCloudServiceFactory(ICloudServiceManager serviceManager, IClusterNodeServerProvider nodeServerProvider) {
    this.serviceManager = serviceManager;
    this.nodeServerProvider = nodeServerProvider;
  }

  @Override
  public @Nullable ServiceInfoSnapshot createCloudService(ServiceConfiguration serviceConfiguration) {
    // check if this node can start services
    if (this.nodeServerProvider.getHeadNode() == this.nodeServerProvider.getSelfNode()) {
      // get the logic node server to start the service on
      IClusterNodeServer nodeServer = this.peekLogicNodeServer(serviceConfiguration);
      // if there is a node server send a request to start a service
      if (nodeServer != null) {
        return this.sendNodeServerStartRequest(
          "head_node_to_node_start_service",
          nodeServer.getNodeInfo().getUniqueId(),
          serviceConfiguration);
      }
      // start the service on the local node
      return this.serviceManager.createLocalCloudService(serviceConfiguration).getServiceInfoSnapshot();
    } else {
      // send a request to the head node to start a service on the best node server
      return this.sendNodeServerStartRequest(
        "node_to_head_start_service",
        this.nodeServerProvider.getHeadNode().getNodeInfo().getUniqueId(),
        serviceConfiguration);
    }
  }

  protected @Nullable ServiceInfoSnapshot sendNodeServerStartRequest(
    @NotNull String channel,
    @NotNull String targetNode,
    @NotNull ServiceConfiguration configuration
  ) {
    // send a request to the node to start a service
    ChannelMessage result = ChannelMessage.builder()
      .channel(channel)
      .target(Type.NODE, targetNode)
      .buffer(DataBufFactory.defaultFactory().createEmpty().writeObject(configuration))
      .build()
      .sendSingleQueryAsync()
      .get(5, TimeUnit.SECONDS, null);
    // read the result service info from the buffer
    return result == null ? null : result.getContent().readObject(ServiceInfoSnapshot.class);
  }

  protected @Nullable IClusterNodeServer peekLogicNodeServer(@NotNull ServiceConfiguration configuration) {
    // extract the max heap memory from the snapshot which will be used for later memory usage comparison
    int mh = configuration.getProcessConfig().getMaxHeapMemorySize();
    // find the best node server
    return this.nodeServerProvider.getNodeServers().stream()
      .filter(IClusterNodeServer::isAvailable)
      .min((left, right) -> {
        // begin by comparing the heap memory usage
        ComparisonChain chain = ComparisonChain.start()
          .compare(left.getNodeInfoSnapshot().getUsedMemory() + mh, right.getNodeInfoSnapshot().getUsedMemory() + mh);
        // only include the cpu usage if both nodes can provide a value
        if (left.getNodeInfoSnapshot().getSystemCpuUsage() >= 0
          && right.getNodeInfoSnapshot().getSystemCpuUsage() >= 0) {
          // add the system usage to the chain
          chain.compare(
            left.getNodeInfoSnapshot().getSystemCpuUsage(),
            right.getNodeInfoSnapshot().getSystemCpuUsage());
        }
        // use the result of the comparison
        return chain.result();
      }).orElse(null);
  }
}
