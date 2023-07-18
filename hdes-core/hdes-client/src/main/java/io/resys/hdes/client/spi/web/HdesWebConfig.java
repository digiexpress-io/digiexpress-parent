package io.resys.hdes.client.spi.web;

/*-
 * #%L
 * hdes-client
 * %%
 * Copyright (C) 2020 - 2022 Copyright 2020 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

public class HdesWebConfig {
  public final static String MODELS = "dataModels";
  public final static String EXPORTS = "exports";
  public final static String COMMANDS = "commands";
  public final static String DEBUGS = "debugs";
  public final static String IMPORTS = "importTag";
  public final static String COPYAS = "copyas";
  public final static String RESOURCES = "resources";
  public final static String HISTORY = "history";

  public final static String VERSION = "version";

  private final String servicePath; // context root for the rest of services
  private final String modelsPath;
  private final String exportsPath;
  private final String commandsPath;
  private final String debugsPath;
  private final String importsPath;
  private final String copyasPath;
  private final String resourcesPath;
  private final String historyPath;

  public HdesWebConfig(String servicePath, String modelsPath, String exportsPath, String commandsPath,
      String debugsPath, String importsPath, String copyasPath, String resourcesPath, String historyPath) {
    super();
    this.servicePath = servicePath;
    this.modelsPath = modelsPath;
    this.exportsPath = exportsPath;
    this.commandsPath = commandsPath;
    this.debugsPath = debugsPath;
    this.importsPath = importsPath;
    this.copyasPath = copyasPath;
    this.resourcesPath = resourcesPath;
    this.historyPath = historyPath;
  }

  public String getServicePath() {
    return servicePath;
  }

  public String getModelsPath() {
    return modelsPath;
  }

  public String getExportsPath() {
    return exportsPath;
  }

  public String getCommandsPath() {
    return commandsPath;
  }

  public String getDebugsPath() {
    return debugsPath;
  }

  public String getImportsPath() {
    return importsPath;
  }

  public String getCopyasPath() {
    return copyasPath;
  }

  public String getResourcesPath() {
    return resourcesPath;
  }

  public String getHistoryPath() {
    return historyPath;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String servicePath; // context root for the rest of services
    private String modelsPath;
    private String exportsPath;
    private String commandsPath;
    private String debugsPath;
    private String importsPath;
    private String copyasPath;
    private String resourcesPath;
    private String historyPath;

    public HdesWebConfig build() {
      return new HdesWebConfig(servicePath, modelsPath, exportsPath, commandsPath, debugsPath, importsPath,
          copyasPath, resourcesPath, historyPath);
    }

    public Builder servicePath(String servicePath) {
      this.servicePath = servicePath;
      return this;
    }

    public Builder modelsPath(String modelsPath) {
      this.modelsPath = modelsPath;
      return this;
    }

    public Builder exportsPath(String exportsPath) {
      this.exportsPath = exportsPath;
      return this;
    }

    public Builder commandsPath(String commandsPath) {
      this.commandsPath = commandsPath;
      return this;
    }

    public Builder debugsPath(String debugsPath) {
      this.debugsPath = debugsPath;
      return this;
    }

    public Builder importsPath(String importsPath) {
      this.importsPath = importsPath;
      return this;
    }

    public Builder copyasPath(String copyasPath) {
      this.copyasPath = copyasPath;
      return this;
    }

    public Builder resourcesPath(String resourcesPath) {
      this.resourcesPath = resourcesPath;
      return this;
    }

    public Builder historyPath(String historyPath) {
      this.historyPath = historyPath;
      return this;
    }
  }
}
