package io.thestencil.quarkus.iam.deployment;

/*-
 * #%L
 * quarkus-stencil-iam-deployment
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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


import io.quarkus.builder.item.SimpleBuildItem;

public final class IAMBuildItem extends SimpleBuildItem {

  private final String servicePath;
  private final String livenessPath;
  private final String rolesPath;
  
  public IAMBuildItem(String servicePath, String livenessPath, String rolesPath) {
    super();
    this.servicePath = servicePath;
    this.livenessPath = livenessPath;
    this.rolesPath = rolesPath;
  }

  public String getServicePath() {
    return servicePath;
  }
  public String getLivenessPath() {
    return livenessPath;
  }
  public String getRolesPath() {
    return rolesPath;
  }

}
