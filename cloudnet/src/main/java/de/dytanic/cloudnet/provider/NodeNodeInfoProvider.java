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

package de.dytanic.cloudnet.provider;

import aerogel.Inject;
import aerogel.Singleton;
import aerogel.auto.Provides;
import com.google.common.base.Preconditions;
import de.dytanic.cloudnet.cluster.IClusterNodeServer;
import de.dytanic.cloudnet.cluster.IClusterNodeServerProvider;
import de.dytanic.cloudnet.driver.command.CommandInfo;
import de.dytanic.cloudnet.driver.network.cluster.NetworkClusterNode;
import de.dytanic.cloudnet.driver.network.cluster.NetworkClusterNodeInfoSnapshot;
import de.dytanic.cloudnet.driver.provider.NodeInfoProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO: re-add command stuff
@Singleton
@Provides(NodeInfoProvider.class)
public class NodeNodeInfoProvider implements NodeInfoProvider {

  private final IClusterNodeServerProvider clusterNodeServerProvider;

  @Inject
  public NodeNodeInfoProvider(IClusterNodeServerProvider clusterNodeServerProvider) {
    this.clusterNodeServerProvider = clusterNodeServerProvider;
  }

  @Override
  public Collection<CommandInfo> getConsoleCommands() {
    // TODO: return this.cloudNet.getCommandMap().getCommandInfos();
    return null;
  }

  @Override
  public NetworkClusterNode[] getNodes() {
    return this.clusterNodeServerProvider.getNodeServers().stream()
      .map(IClusterNodeServer::getNodeInfo)
      .toArray(NetworkClusterNode[]::new);
  }

  @Nullable
  @Override
  public NetworkClusterNode getNode(@NotNull String uniqueId) {
    Preconditions.checkNotNull(uniqueId);
    // check if the current node is requested
    if (uniqueId.equals(this.clusterNodeServerProvider.getSelfNode().getNodeInfo().getUniqueId())) {
      return this.clusterNodeServerProvider.getSelfNode().getNodeInfo();
    }
    // find the node info
    return this.clusterNodeServerProvider.getNodeServers().stream()
      .map(IClusterNodeServer::getNodeInfo)
      .filter(nodeInfo -> nodeInfo.getUniqueId().equals(uniqueId))
      .findFirst()
      .orElse(null);
  }

  @Override
  public NetworkClusterNodeInfoSnapshot[] getNodeInfoSnapshots() {
    return this.clusterNodeServerProvider.getNodeServers().stream()
      .map(IClusterNodeServer::getNodeInfoSnapshot)
      .filter(Objects::nonNull)
      .toArray(NetworkClusterNodeInfoSnapshot[]::new);
  }

  @Nullable
  @Override
  public NetworkClusterNodeInfoSnapshot getNodeInfoSnapshot(@NotNull String uniqueId) {
    // check if the current node is requested
    if (uniqueId.equals(this.clusterNodeServerProvider.getSelfNode().getNodeInfo().getUniqueId())) {
      return this.clusterNodeServerProvider.getSelfNode().getNodeInfoSnapshot();
    }
    // find the node we are looking for
    return this.clusterNodeServerProvider.getNodeServers().stream()
      .filter(nodeServer -> nodeServer.getNodeInfo().getUniqueId().equals(uniqueId))
      .map(IClusterNodeServer::getNodeInfoSnapshot)
      .filter(Objects::nonNull)
      .findFirst()
      .orElse(null);
  }

  @Override
  public String[] sendCommandLine(@NotNull String commandLine) {
    Preconditions.checkNotNull(commandLine);

    return new String[0];
  }

  @Override
  public String[] sendCommandLine(@NotNull String nodeUniqueId, @NotNull String commandLine) {
    Preconditions.checkNotNull(nodeUniqueId);
    Preconditions.checkNotNull(commandLine);
    // check if we should execute the command on the current node
    if (nodeUniqueId.equals(this.clusterNodeServerProvider.getSelfNode().getNodeInfo().getUniqueId())) {
      return this.sendCommandLine(commandLine);
    }
    // find the node server and execute the command on there
    IClusterNodeServer clusterNodeServer = this.clusterNodeServerProvider.getNodeServer(nodeUniqueId);
    if (clusterNodeServer != null && clusterNodeServer.isConnected()) {
      return clusterNodeServer.sendCommandLine(commandLine);
    }
    // unable to execute the command
    return null;
  }

  private void sendCommandLine0(Collection<String> collection, String commandLine) {
    //  this.cloudNet.getCommandMap().dispatchCommand(new DriverCommandSender(collection), commandLine);
  }

  @Nullable
  @Override
  public CommandInfo getConsoleCommand(@NotNull String commandLine) {
    //  Command command = this.cloudNet.getCommandMap().getCommandFromLine(commandLine);
    //  return command != null ? command.getInfo() : null;
    return null;
  }

  @Override
  public Collection<String> getConsoleTabCompleteResults(@NotNull String commandLine) {
    // return this.cloudNet.getCommandMap().tabCompleteCommand(commandLine);
    return Collections.emptyList();
  }
}
