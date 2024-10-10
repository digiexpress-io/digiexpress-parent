package io.thestencil.quarkus.feedback;

import java.util.List;

/*-
 * #%L
 * quarkus-stencil-user-actions
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

import io.thestencil.iam.api.UserActionsClient;

public class FeedbackContext {
  private final UserActionsClient client;
  private final List<String> allowed;
  private final String userId;
  private final String userName;
  private final String firstName;
  private final String lastName;
  private final String email;
  private final String address;
  private final String allowedPath;
  
  public FeedbackContext(
      UserActionsClient client,
      String allowedPath,
      List<String> allowed, 
      String userName, String userId,
      String firstName, String lastName,
      String email, String address) {
    super();
    this.allowedPath = allowedPath;
    this.client = client;
    this.allowed = allowed;
    this.userName = userName;
    this.userId = userId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.address = address;
  }
  public String getUserName() {
    return userName;
  }
  public List<String> getAllowed() {
    return allowed;
  }
  public UserActionsClient getClient() {
    return client;
  }
  public String getFillPath() {
    return client.config().getFillPath();
  }
  public String getServicePath() {
    return client.config().getServicePath();
  }
  public String getUserId() {
    return userId;
  }
  public String getFirstName() {
    return firstName;
  }
  public String getLastName() {
    return lastName;
  }
  public String getEmail() {
    return email;
  }
  public String getAddress() {
    return address;
  }
  public String getAllowedPath() {
    return allowedPath;
  }

}
