/*
 * Copyright 2019-2023 CloudNetService team & contributors
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

package eu.cloudnetservice.driver.util;

import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;
import org.jetbrains.annotations.ApiStatus;

/**
 * Utility class to create concurrent hash maps with default table settings.
 *
 * @since 4.0
 */
@ApiStatus.Internal
public final class ConcurrencyUtil {

  private ConcurrencyUtil() {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates a new concurrent hash map with the following options set:
   * <ul>
   *   <li>initialCapacity: 16</li>
   *   <li>loadFactory: 0.9</li>
   *   <li>concurrencyLevel: 1</li>
   * </ul>
   * Use this method to create a concurrent map that has improved performance while being concurrent.
   * Due to the low concurrency level do not use this map for any very highly concurrent application with frequent updates of the map.
   *
   * @param <K> the key type of the map.
   * @param <V> the value type of the map.
   * @return a new concurrent hash map with the mentioned options.
   * @see ConcurrentHashMap
   */
  public static <K, V> @NonNull ConcurrentHashMap<K, V> createConcurrentMap() {
    return createConcurrentMap(1);
  }

  /**
   * Creates a new concurrent hash map with the following options set:
   * <ul>
   *   <li>initialCapacity: 16</li>
   *   <li>loadFactory: 0.9</li>
   * </ul>
   * Use this method to create a concurrent map that has improved performance while being concurrent.
   * Keep in mind that a low concurrency level should not be used in a very highly concurrent application with frequent updates of the map.
   *
   * @param <K>              the key type of the map.
   * @param <V>              the value type of the map.
   * @param concurrencyLevel the concurrency level of the created map.
   * @return a new concurrent hash map with the mentioned options.
   * @see ConcurrentHashMap
   */
  public static <K, V> @NonNull ConcurrentHashMap<K, V> createConcurrentMap(int concurrencyLevel) {
    return new ConcurrentHashMap<>(16, 0.9f, concurrencyLevel);
  }
}
