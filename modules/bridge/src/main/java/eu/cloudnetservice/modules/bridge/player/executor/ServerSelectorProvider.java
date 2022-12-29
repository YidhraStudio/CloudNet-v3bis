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

package eu.cloudnetservice.modules.bridge.player.executor;

import eu.cloudnetservice.common.StringUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

/**
 *
 */
public class ServerSelectorProvider {

  private final Map<String, ServerSelectorType> serverSelectorTypes = new HashMap<>();

  public ServerSelectorProvider() {
    this.registerServerSelectorType(ServerSelectorType.HIGHEST_PLAYERS);
    this.registerServerSelectorType(ServerSelectorType.LOWEST_PLAYERS);
    this.registerServerSelectorType(ServerSelectorType.RANDOM);
  }

  /**
   *
   * @param type
   */
  public void registerServerSelectorType(@NonNull ServerSelectorType type) {
    this.serverSelectorTypes.put(StringUtil.toUpper(type.name()), type);
  }

  /**
   *
   * @param name
   * @return
   */
  public @Nullable ServerSelectorType serverSelectorType(@NonNull String name) {
    return this.serverSelectorTypes.get(StringUtil.toUpper(name));
  }

  /**
   *
   * @return
   */
  @UnmodifiableView
  public @NonNull Map<String, ServerSelectorType> knownServerSelectorTypes() {
    return Collections.unmodifiableMap(this.serverSelectorTypes);
  }
}
