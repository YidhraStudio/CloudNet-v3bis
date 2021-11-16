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

package de.dytanic.cloudnet.ext.simplenametags;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.event.EventListener;
import de.dytanic.cloudnet.driver.event.events.permission.PermissionUpdateGroupEvent;
import de.dytanic.cloudnet.driver.event.events.permission.PermissionUpdateUserEvent;
import de.dytanic.cloudnet.driver.permission.PermissionUser;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.jetbrains.annotations.NotNull;

public final class CloudSimpleNameTagsListener<P> {

  private final Executor syncTaskExecutor;
  private final SimpleNameTagsManager<P> nameTagsManager;

  public CloudSimpleNameTagsListener(
    @NotNull Executor syncTaskExecutor,
    @NotNull SimpleNameTagsManager<P> nameTagsManager
  ) {
    this.syncTaskExecutor = syncTaskExecutor;
    this.nameTagsManager = nameTagsManager;
  }

  @EventListener
  public void handle(PermissionUpdateUserEvent event) {
    this.syncTaskExecutor.execute(() -> {
      // get the player if online
      P player = this.nameTagsManager.getOnlinePlayer(event.getPermissionUser().getUniqueId());
      if (player != null) {
        // update the name tag of the player
        this.nameTagsManager.updateNameTagsFor(player);
      }
    });
  }

  @EventListener
  public void handle(PermissionUpdateGroupEvent event) {
    this.syncTaskExecutor.execute(() -> {
      // find all matching players
      for (P player : this.nameTagsManager.getOnlinePlayers()) {
        UUID playerUniqueId = this.nameTagsManager.getPlayerUniqueId(player);
        // get the associated user
        PermissionUser user = CloudNetDriver.getInstance().getPermissionManagement().getUser(playerUniqueId);
        if (user != null && user.inGroup(event.getPermissionGroup().getName())) {
          this.nameTagsManager.updateNameTagsFor(player);
        }
      }
    });
  }
}