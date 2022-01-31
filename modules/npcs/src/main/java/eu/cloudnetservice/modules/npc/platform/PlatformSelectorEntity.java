/*
 * Copyright 2019-2022 CloudNetService team & contributors
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

package eu.cloudnetservice.modules.npc.platform;

import eu.cloudnetservice.cloudnet.driver.service.ServiceInfoSnapshot;
import eu.cloudnetservice.modules.npc.NPC;
import eu.cloudnetservice.modules.npc.NPC.ClickAction;
import java.util.Set;
import lombok.NonNull;

public interface PlatformSelectorEntity<L, P, M, I> {

  void spawn();

  void remove();

  void update();

  void trackService(@NonNull ServiceInfoSnapshot service);

  void stopTrackingService(@NonNull ServiceInfoSnapshot service);

  void handleRightClickAction(@NonNull P player);

  void handleLeftClickAction(@NonNull P player);

  void executeAction(@NonNull P player, @NonNull ClickAction action);

  @NonNull I selectorInventory();

  void handleInventoryInteract(@NonNull I inv, @NonNull P player, @NonNull M clickedItem);

  @NonNull NPC npc();

  @NonNull L location();

  int entityId();

  @NonNull Set<Integer> infoLineEntityIds();

  @NonNull String scoreboardRepresentation();

  boolean spawned();

  boolean canSpawn();
}
