package io.thestencil.quarkus.useractions.deployment;

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


public final class UserActionsBuildItem extends SimpleBuildItem {
  
  private final String servicePath;
  private final String fillPath;
  private final String reviewPath;
  private final String messagesPath;
  private final String attachmentsPath;
  private final String authorizationsPath;

  public UserActionsBuildItem(
		  String servicePath, String fillPath, String reviewPath, String messagesPath, String attachmentsPath, String authorizationsPath) {
    super();
    this.servicePath = servicePath;
    this.fillPath = fillPath;
    this.reviewPath = reviewPath;
    this.messagesPath = messagesPath;
    this.attachmentsPath = attachmentsPath;
    this.authorizationsPath = authorizationsPath;
  }

  public String getServicePath() {
    return servicePath;
  }
  public String getFillPath() {
    return fillPath;
  }
  public String getReviewPath() {
    return reviewPath;
  }
  public String getMessagesPath() {
    return messagesPath;
  }
  public String getAttachmentsPath() {
    return attachmentsPath;
  }

  public String getAuthorizationsPath() {
    return authorizationsPath;
  }
}
