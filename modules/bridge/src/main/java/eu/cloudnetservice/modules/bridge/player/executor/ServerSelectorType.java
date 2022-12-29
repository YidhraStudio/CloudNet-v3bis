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

import com.google.common.base.Preconditions;
import eu.cloudnetservice.common.Nameable;
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot;
import eu.cloudnetservice.modules.bridge.BridgeServiceProperties;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

/**
 * The server selector type provides several constants to compare services based conditions.
 *
 * @since 4.0
 */
@ToString
@EqualsAndHashCode
public class ServerSelectorType implements Nameable {

  /**
   * The service with the highest player count is preferred.
   */
  public static final ServerSelectorType HIGHEST_PLAYERS = ServerSelectorType.builder()
    .name("HIGHEST_PLAYERS")
    .comparator(Comparator.comparingInt(o -> o.propertyOr(BridgeServiceProperties.ONLINE_COUNT, 0)))
    .build();

  /**
   * The service with the lowest player count is preferred.
   */
  public static final ServerSelectorType LOWEST_PLAYERS = ServerSelectorType.builder()
    .name("LOWEST_PLAYERS")
    .comparator((o1, o2) -> Integer.compare(
      o2.propertyOr(BridgeServiceProperties.ONLINE_COUNT, 0),
      o1.propertyOr(BridgeServiceProperties.ONLINE_COUNT, 0)))
    .build();

  /**
   * A random service is chosen.
   */
  public static final ServerSelectorType RANDOM = ServerSelectorType.builder()
    .name("RANDOM")
    .comparator(Comparator.comparingInt(value -> ThreadLocalRandom.current().nextInt(2) - 1))
    .build();

  private final String name;
  private final Comparator<ServiceInfoSnapshot> comparator;

  /**
   * Creates a new server selector type enum constant with the given service comparator.
   *
   * @param comparator the comparator to apply to the services.
   * @throws NullPointerException if the comparator is null.
   */
  protected ServerSelectorType(@NonNull String name, @NonNull Comparator<ServiceInfoSnapshot> comparator) {
    this.name = name;
    this.comparator = comparator;
  }

  /**
   * Constructs a new builder for a server selector type.
   *
   * @return a new server selector builder.
   */
  public static @NonNull Builder builder() {
    return new Builder();
  }

  /**
   * Constructs a new builder for a server selector type which has the same properties as the given selector type.
   * <p>
   * When calling build directly after constructing a builder using this method, it will result in a server selector
   * which is equal but not the same as the given one.
   *
   * @param type the server selector type to copy the properties of.
   * @return a builder for a server selector type with the properties of the given one already set.
   * @throws NullPointerException if the given type is null.
   */
  public static @NonNull Builder builder(@NonNull ServerSelectorType type) {
    return builder().name(type.name()).comparator(type.comparator());
  }

  /**
   * {@inheritDoc}
   */
  public @NonNull String name() {
    return this.name;
  }

  /**
   * Gets the comparator used to sort the services infos according to the server selector type.
   *
   * @return the comparator for the server selector.
   */
  public @NonNull Comparator<ServiceInfoSnapshot> comparator() {
    return this.comparator;
  }

  /**
   * A builder for a server selector.
   *
   * @since 4.0
   */
  public static class Builder {

    private String name;
    private Comparator<ServiceInfoSnapshot> comparator;

    /**
     * Sets the name of the server selector type.
     *
     * @param name the name of the server selector type to use.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if the given name is null.
     */
    public @NonNull Builder name(@NonNull String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets the service info comparator for the server selector type.
     *
     * @param comparator the comparator of the server selector type to use.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if the given comparator is null.
     */
    public @NonNull Builder comparator(@NonNull Comparator<ServiceInfoSnapshot> comparator) {
      this.comparator = comparator;
      return this;
    }

    /**
     * Builds the new server selector type based on the set properties.
     *
     * @return the created server selector type.
     * @throws NullPointerException if either the name or the comparator is null.
     */
    public @NonNull ServerSelectorType build() {
      Preconditions.checkNotNull(this.name, "Missing server selector name");
      Preconditions.checkNotNull(this.comparator, "Missing server selector comparator");

      return new ServerSelectorType(this.name, this.comparator);
    }
  }
}
