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

package de.dytanic.cloudnet.launcher.version.update;

import de.dytanic.cloudnet.launcher.LauncherUtils;
import de.dytanic.cloudnet.launcher.version.VersionInfo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Updater extends VersionInfo {

  boolean init(Path versionDirectory, String githubRepository);

  boolean installFile(String name, Path path);

  default boolean installUpdate(String moduleDestinationBaseDirectory) {
    var successful = false;

    if (this.getCurrentVersion() != null) {

      try {
        Files.createDirectories(this.getTargetDirectory());

        successful = true;

        for (var versionFile : LauncherUtils.VERSION_FILE_NAMES) {
          if (!this.installFile(versionFile, this.getTargetDirectory().resolve(versionFile))) {
            successful = false;
          }
        }

        if (moduleDestinationBaseDirectory != null) {
          var moduleDirectoryPath = Path.of(moduleDestinationBaseDirectory);

          var modulesExist = Files.exists(moduleDirectoryPath);

          if (!modulesExist) {
            Files.createDirectories(moduleDirectoryPath);
          }

          for (var module : LauncherUtils.DEFAULT_MODULES) {
            var modulePath = moduleDirectoryPath.resolve(module.getFileName());

            // avoiding the installation of manual removed modules
            if (!modulesExist || Files.exists(modulePath)) {
              //CHECKSTYLE.OFF: launcher has no special logger
              System.out.printf("Installing module %s...%n", module.getName());
              //CHECKSTYLE.ON

              if (!this.installFile(module.getFileName(), modulePath)) {
                successful = false;
              }
            }
          }
        }
      } catch (IOException exception) {
        //CHECKSTYLE.OFF: launcher has no special logger
        exception.printStackTrace();
        //CHECKSTYLE.ON
        return false;
      }

    }

    return successful;
  }

}