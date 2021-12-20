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

package de.dytanic.cloudnet.ext.bridge.platform.nukkit;

import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerLoginEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.plugin.Plugin;
import de.dytanic.cloudnet.ext.bridge.platform.PlatformBridgeManagement;
import de.dytanic.cloudnet.ext.bridge.platform.helper.ServerPlatformHelper;
import de.dytanic.cloudnet.wrapper.Wrapper;
import lombok.NonNull;

final class NukkitPlayerManagementListener implements Listener {

  private final Plugin plugin;
  private final PlatformBridgeManagement<?, ?> management;

  public NukkitPlayerManagementListener(@NonNull Plugin plugin, @NonNull PlatformBridgeManagement<?, ?> management) {
    this.plugin = plugin;
    this.management = management;
  }

  @EventHandler
  public void handle(@NonNull PlayerLoginEvent event) {
    var task = this.management.selfTask();
    // check if the current task is present
    if (task != null) {
      // check if maintenance is activated
      if (task.maintenance() && !event.getPlayer().hasPermission("cloudnet.bridge.maintenance")) {
        event.setCancelled(true);
        event.setKickMessage(this.management.configuration().message(
          event.getPlayer().getLocale(),
          "server-join-cancel-because-maintenance"));
        return;
      }
      // check if a custom permission is required to join
      var permission = task.properties().getString("requiredPermission");
      if (permission != null && !event.getPlayer().hasPermission(permission)) {
        event.setCancelled(true);
        event.setKickMessage(this.management.configuration().message(
          event.getPlayer().getLocale(),
          "server-join-cancel-because-permission"));
      }
    }
  }

  @EventHandler
  public void handle(@NonNull PlayerJoinEvent event) {
    ServerPlatformHelper.sendChannelMessageLoginSuccess(
      event.getPlayer().getUniqueId(),
      this.management.ownNetworkServiceInfo());
    // update the service info in the next tick
    Server.getInstance().getScheduler().scheduleTask(this.plugin, Wrapper.instance()::publishServiceInfoUpdate);
  }

  @EventHandler
  public void handle(@NonNull PlayerQuitEvent event) {
    ServerPlatformHelper.sendChannelMessageDisconnected(
      event.getPlayer().getUniqueId(),
      this.management.ownNetworkServiceInfo());
    // update the service info in the next tick
    Server.getInstance().getScheduler().scheduleTask(this.plugin, Wrapper.instance()::publishServiceInfoUpdate);
  }
}