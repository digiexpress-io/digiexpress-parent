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

import io.thestencil.iam.api.ImmutableUserActionsClientConfig;
import io.thestencil.iam.api.UserActionsClient;
import io.vertx.core.http.RequestOptions;


public class UserActionsClientDefault implements UserActionsClient {
  private final RequestOptions process;
  private final RequestOptions fill;
  private final RequestOptions review;
  private final RequestOptions tasks;
  private final RequestOptions attachment;
  private final UserActionsClientConfig config;
  
  public UserActionsClientDefault(UserActionsClientConfig config) {
    super();
    this.process = new RequestOptions()
        .setURI(config.getProcesses().getPath())
        .setHost(config.getProcesses().getHost());
    this.fill = new RequestOptions()
        .setURI(config.getFill().getPath())
        .setHost(config.getFill().getHost());
    this.review = new RequestOptions()
        .setURI(config.getReview().getPath())
        .setHost(config.getReview().getHost());
    this.tasks = new RequestOptions()
        .setURI(config.getReplyTo().getPath())
        .setHost(config.getReplyTo().getHost());
    this.attachment = new RequestOptions()
        .setURI(config.getAttachments().getPath())
        .setHost(config.getAttachments().getHost());
    this.config = config;
  }
  @Override
  public UserActionBuilder createUserAction() {
    return new UserActionBuilderDefault(process, config);
  }
  @Override
  public UserActionQuery queryUserAction() {
    return new UserActionQueryDefault(process, config, 
        () -> new MessagesQueryBuilderDefault(tasks, config),
        () -> queryAttachments(),
        () -> new TaskQueryBuilderDefault(tasks, config)
        );
  }
  @Override
  public MarkUserActionBuilder markUser() {
    return new MarkUserActionBuilderDefault(tasks, config, () -> queryUserAction());
  }
  @Override
  public FillBuilder fill() {
    return new DefaultFillBuilder(fill, config);
  }
  @Override
  public ReviewBuilder review() {
    return new DefaultReviewBuilder(review, config);
  }
  @Override
  public CancelUserActionBuilder cancelUserAction() {
    return new CancelUserActionBuilderDefault(process, config, () -> queryUserAction());
  }
  @Override
  public ReplyToBuilder replyTo() {
    return new ReplyToBuilderDefault(tasks, config, () -> queryUserAction());
  }
  @Override
  public UserActionsClientConfig config() {
    return config;
  }
  @Override
  public AttachmentBuilder attachment() {
    return new AttachmentBuilderDefault(attachment, config, () -> queryUserAction());
  }
  @Override
  public AttachmentQuery queryAttachments() {
    return new AttachmentQueryDefault(attachment, config);
  }
  @Override
  public AttachmentDownloadBuilder attachmentDownload() {
    return new AttachmentDownloadBuilderDefault(attachment, config, () -> queryUserAction());
  }
  @Override
  public AuthorizationActionQuery authorizationActionQuery() {
    return new AuthorizationActionQueryDefault(process, config);
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
    public UserActionsClientDefault build() {
      return new UserActionsClientDefault(config.build());
    }
  }
}
