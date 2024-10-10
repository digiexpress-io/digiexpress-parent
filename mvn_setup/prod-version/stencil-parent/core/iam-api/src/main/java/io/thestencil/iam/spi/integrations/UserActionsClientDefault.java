package io.thestencil.iam.spi.integrations;

/*-
 * #%L
 * iam-api
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

import java.util.function.Function;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.thestencil.iam.api.ImmutableUserActionsClientConfig;
import io.thestencil.iam.api.UserActionsClient;
import io.thestencil.iam.spi.support.RemoteIntegrationConverter;
import io.vertx.core.http.RequestOptions;


public class UserActionsClientDefault implements UserActionsClient {
  private final RequestOptions process;
  private final RequestOptions fill;
  private final RequestOptions review;
  private final RequestOptions tasks;
  private final RequestOptions attachment;
  private final UserActionsClientConfig config;
  private final JsonWebToken idToken;
  
  public UserActionsClientDefault(UserActionsClientConfig config, JsonWebToken idToken) {
    super();
    this.process = RemoteIntegrationConverter.integrationToOptions(config.getProcesses());
    this.fill = RemoteIntegrationConverter.integrationToOptions(config.getFill());
    this.review = config.getReview() != null ? RemoteIntegrationConverter.integrationToOptions(config.getReview()) : null;
    this.tasks = config.getReview() != null ? RemoteIntegrationConverter.integrationToOptions(config.getReplyTo()) : null;
    this.attachment = config.getReview() != null ? RemoteIntegrationConverter.integrationToOptions(config.getAttachments()) : null;
    
    this.config = config;
    this.idToken = idToken;
  }
  
  @Override
  public UserActionBuilder createUserAction() {
    return new UserActionBuilderDefault(process, config, idToken);
  }
  @Override
  public UserActionQuery queryUserAction() {
    return new UserActionQueryDefault(process, config, 
        () -> new MessagesQueryBuilderDefault(tasks, config, idToken),
        () -> queryAttachments(),
        () -> new TaskQueryBuilderDefault(tasks, config, idToken),
        idToken);
  }
  @Override
  public MarkUserActionBuilder markUser() {
    return new MarkUserActionBuilderDefault(tasks, config, () -> queryUserAction(), idToken);
  }
  @Override
  public FillBuilder fill() {
    return new DefaultFillBuilder(fill, config, idToken);
  }
  @Override
  public ReviewBuilder review() {
    return new DefaultReviewBuilder(review, config, idToken);
  }
  @Override
  public CancelUserActionBuilder cancelUserAction() {
    return new CancelUserActionBuilderDefault(process, config, () -> queryUserAction(), idToken);
  }
  @Override
  public ReplyToBuilder replyTo() {
    return new ReplyToBuilderDefault(tasks, config, () -> queryUserAction(), idToken);
  }
  @Override
  public UserActionsClientConfig config() {
    return config;
  }
  @Override
  public AttachmentBuilder attachment() {
    return new AttachmentBuilderDefault(attachment, config, () -> queryUserAction(), idToken);
  }
  @Override
  public AttachmentQuery queryAttachments() {
    return new AttachmentQueryDefault(attachment, config, idToken);
  }
  @Override
  public AttachmentDownloadBuilder attachmentDownload() {
    return new AttachmentDownloadBuilderDefault(attachment, config, () -> queryUserAction(), idToken);
  }
  @Override
  public AuthorizationActionQuery authorizationActionQuery() {
    return new AuthorizationActionQueryDefault(process, config, idToken);
  }
  public static Builder builder() {
    return new Builder();
  }
  public static class Builder {
    private ImmutableUserActionsClientConfig.Builder config = ImmutableUserActionsClientConfig.builder();
    public Builder config(Function<ImmutableUserActionsClientConfig.Builder, ImmutableUserActionsClientConfig.Builder> c) {
      this.config = c.apply(config);
      return this;
    }
    public UserActionsClientDefault build(JsonWebToken idToken) {
      return new UserActionsClientDefault(config.build(), idToken);
    }
  }
}
