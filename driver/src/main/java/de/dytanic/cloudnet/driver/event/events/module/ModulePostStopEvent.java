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

package de.dytanic.cloudnet.driver.event.events.module;

import de.dytanic.cloudnet.driver.module.IModuleProvider;
import de.dytanic.cloudnet.driver.module.IModuleWrapper;
import de.dytanic.cloudnet.driver.module.ModuleLifeCycle;
import lombok.NonNull;

/**
 * This event is being called after a module has been stopped and the tasks with the lifecycle {@link
 * ModuleLifeCycle#STOPPED} of this module have been fired. {@link IModuleWrapper#moduleLifeCycle()} is still {@link
 * ModuleLifeCycle#STARTED} or {@link ModuleLifeCycle#LOADED}.
 */
public final class ModulePostStopEvent extends ModuleEvent {

  public ModulePostStopEvent(@NonNull IModuleProvider moduleProvider, @NonNull IModuleWrapper module) {
    super(moduleProvider, module);
  }
}