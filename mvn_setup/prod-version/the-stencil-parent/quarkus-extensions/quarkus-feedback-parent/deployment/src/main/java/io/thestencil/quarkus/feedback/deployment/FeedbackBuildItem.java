package io.thestencil.quarkus.feedback.deployment;

/*-
 * #%L
 * quarkus-stencil-user-actions-deployment
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


public final class FeedbackBuildItem extends SimpleBuildItem {
  
  private final String servicePath;
  private final String fillPath;
  private final String allowedPath;


  public FeedbackBuildItem(
		  String servicePath, String fillPath, String allowedPath) {
    super();
    this.servicePath = servicePath;
    this.fillPath = fillPath;
    this.allowedPath = allowedPath;
  }

  public String getServicePath() {
    return servicePath;
  }
  public String getFillPath() {
    return fillPath;
  }

  public String getAllowedPath() {
    return allowedPath;
  }
}
