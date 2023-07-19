package io.thestencil.site.pg;

/*-
 * #%L
 * quarkus-stencil-ide-services-deployment
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

public final class SiteBuildItem extends SimpleBuildItem {
  
  private final String servicePath;
  
  public SiteBuildItem(String servicePath) {
    super();
    this.servicePath = servicePath;
  }
  public String getServicePath() {
    return servicePath;
  }
  
  
  public static Builder builder(String servicePath) {
    return new Builder(servicePath);
  }
  
  public static class Builder {
    private final String servicePath;
    public Builder(String servicePath) {
      super();
      this.servicePath = "/" + servicePath;
    }
    public SiteBuildItem build() {
      return new SiteBuildItem(servicePath);
    }
  }
}