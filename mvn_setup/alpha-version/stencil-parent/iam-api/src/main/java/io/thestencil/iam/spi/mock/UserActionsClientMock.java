package io.thestencil.iam.spi.mock;

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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.ImmutableAttachment;
import io.thestencil.iam.api.ImmutableAuthorizationAction;
import io.thestencil.iam.api.ImmutableUserAction;
import io.thestencil.iam.api.ImmutableUserActionsClientConfig;
import io.thestencil.iam.api.ImmutableUserMessage;
import io.thestencil.iam.api.UserActionsClient;
import io.thestencil.iam.spi.support.PortalAssert;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.WebClient;


public class UserActionsClientMock implements UserActionsClient {
  
  private final MockClient dialobClient;
  private final UserActionsClientConfig config;
  
  public UserActionsClientMock(WebClient client, UserActionsClientConfig config, String formId, String apiKey) {
    this.dialobClient = new MockClient(client, config, formId, apiKey);
    this.config = config;
  }
  @Override
  public CancelUserActionBuilder cancelUserAction() {
    return new CancelUserActionBuilder() {
      private String processId;
      @Override
      public CancelUserActionBuilder userId(String userId) {
        return this;
      }
      @Override
      public CancelUserActionBuilder processId(String processId) {
        this.processId = processId;
        return this;
      }
      @Override
      public Uni<UserAction> build() {
        return getDialobClient().deleteQuestionnaire(processId).onItem().transform(e -> 
          ImmutableUserAction.builder()
            .id("mock-id")
            .name("mock-test")
            .viewed(true)
            .reviewUri(config.getReviewPath())
            .formUri(config.getFillPath())
            .formId(processId)
            .formInProgress(true)
            .status("open")
            .build());
      }
      @Override
      public CancelUserActionBuilder userName(String userName) {

        return this;
      }
    };
  }
  @Override
  public UserActionBuilder createUserAction() {
    return new UserActionBuilder() {
      @Override
      public UserActionBuilder userName(String firstName, String lastName) {
        return this;
      }
      @Override
      public UserActionBuilder userId(String userId) {
        return this;
      }
      @Override
      public UserActionBuilder email(String email) {
        return this;
      }
      @Override
      public UserActionBuilder address(String address) {
        return this;
      }
      @Override
      public UserActionBuilder actionName(String actionName) {
        return this;
      }
      @Override
      public UserActionBuilder language(String language) {
        return this;
      }
      @Override
      public UserActionBuilder protectionOrder(Boolean protectionOrder) {
        return this;
      }
      @Override
      public Uni<UserAction> build() {
        return dialobClient.create();
      }
      @Override
      public UserActionBuilder representative(String representativeFirstName,
          String representativeLastName, String representativeUserId) {
        return this;
      }
      @Override
      public UserActionBuilder companyName(String companyName) {
        return this;
      }
    };
  }
  @Override
  public UserActionQuery queryUserAction() {
    return new UserActionQuery() {
      @Override
      public UserActionQuery userId(String userId) {
        return this;
      }
      @Override
      public Multi<UserAction> list() {
        final var action_1 = dialobClient.open().onItem().transform(open ->
          open.stream().map(e -> (UserAction) ImmutableUserAction.builder()
              .from(e)
              .name("covid-test")
              .build())
          .collect(Collectors.toList())
        );
        final var action_2 = dialobClient.completed().onItem().transform(open -> 
          open.stream().map(e -> (UserAction) ImmutableUserAction.builder()
              .from(e)
              .name("covid-test")
              .build())
          .collect(Collectors.toList())
        );
        return Uni.combine().all().unis(action_1, action_2).asTuple()
            .onItem().transformToMulti(tuple -> Multi.createFrom().items(Stream
                .concat(tuple.getItem1().stream(), tuple.getItem2().stream())));
      }
      @Override
      public UserActionQuery processId(String processId) {
        return this;
      }
      @Override
      public UserActionQuery limit(Integer limit) {
        return this;
      }
      @Override
      public UserActionQuery userName(String userName) {
        return this;
      }
      @Override
      public UserActionQuery representativeUserName(String userName) {
        return this;
      }
    };
  }

  @Override
  public MarkUserActionBuilder markUser() {
    return new MarkUserActionBuilder() {
      @Override
      public MarkUserActionBuilder userId(String userId) {
        return this;
      }
      @Override
      public MarkUserActionBuilder processId(String processId) {
        return this;
      }
      @Override
      public Uni<List<UserMessage>> build() {
        return Uni.createFrom().item(Collections.emptyList());
      }
      @Override
      public MarkUserActionBuilder userName(String userName) {
        return this;
      }
    };
  }
  
  @Override
  public FillBuilder fill() {
    return new FillBuilder() {
      private String path;
      private HttpMethod method;
      private Buffer body;
      @Override
      public FillBuilder path(String path) {
        this.path = path;
        return this;
      }
      @Override
      public FillBuilder method(HttpMethod method) {
        this.method = method;
        return this;
      }
      @Override
      public FillBuilder body(Buffer body) {
        this.body = body;
        return this;
      }
      @Override
      public Uni<Buffer> build() {
        return dialobClient.fill(method, path, body);
      }
    };
  }
  @Override
  public ReviewBuilder review() {
    return new ReviewBuilder() {
      private String path;
      @Override
      public ReviewBuilder path(String path) {
        this.path = path;
        return this;
      }
      @Override
      public Uni<Buffer> build() {
        return dialobClient.review(path);
      }
    };
  }
  
  @Override
  public ReplyToBuilder replyTo() {
    return new ReplyToBuilder() {
      @Override
      public ReplyToBuilder userId(String userId) {
        return this;
      }
      @Override
      public ReplyToBuilder text(String text) {
        return this;
      }
      @Override
      public ReplyToBuilder replyToId(String replyToId) {
        return this;
      }
      @Override
      public ReplyToBuilder processId(String processId) {
        return this;
      }
      @Override
      public ReplyToBuilder userName(String userName) {
        return this;
      }
      @Override
      public Uni<UserMessage> build() {
        return Uni.createFrom().item(ImmutableUserMessage.builder()
          .id("")
          .taskId("")
          .created("")
          .userName("")
          .replyToId("")
          .commentText("")
          .build());
      }
    };
  }
  
  @Override
  public UserActionsClientConfig config() {
    return config;
  }
  
  public static Builder builder() {
    return new Builder();
  }
  public static class Builder {
    private String formId;
    private String apiKey;
    private WebClient webClient;
    private ImmutableUserActionsClientConfig.Builder config = ImmutableUserActionsClientConfig.builder();
    public Builder webClient(WebClient webClient) {
      this.webClient = webClient;
      return this;
    }
    public Builder setFormId(String formId) {
      this.formId = formId;
      return this;
    }
    public Builder setApiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }
    public Builder config(Function<ImmutableUserActionsClientConfig.Builder, ImmutableUserActionsClientConfig.Builder> c) {
      this.config = c.apply(config);
      return this;
    }
    public UserActionsClientMock build() {
      return new UserActionsClientMock(webClient, config.build(), formId, apiKey);
    }
  }
  public MockClient getDialobClient() {
    return dialobClient;
  }
  @Override
  public AttachmentBuilder attachment() {
    return new AttachmentBuilder() {
      private String processId;
      private String userName;
      private String userId;
      private String data;
      @Override
      public AttachmentBuilder userName(String userName) {
        this.userName = userName;
        return this;
      }
      @Override
      public AttachmentBuilder userId(String userId) {
        this.userId = userId;
        return this;
      }
      @Override
      public AttachmentBuilder processId(String processId) {
        this.processId = processId;
        return this;
      }
      @Override
      public AttachmentBuilder data(String name, String fileType) {
        this.data = name + fileType;
        return this;
      }
      @Override
      public Multi<Attachment> build() {
        PortalAssert.isTrue(data != null, () -> "data must be defined!");
        PortalAssert.notEmpty(processId, () -> "processId must be defined!");
        PortalAssert.notEmpty(userName, () -> "userName must be defined!");
        PortalAssert.notEmpty(userId, () -> "userId must be defined!");
        
        return Multi.createFrom().item(ImmutableAttachment.builder()
            .id("mock-id")
            .name("mock-file-that-is-not-real")
            .created(LocalDateTime.now().toString())
            .processId(processId)
            .build());
      }
      @Override
      public AttachmentBuilder call(Consumer<AttachmentBuilder> callback) {
        callback.accept(this);
        return this;
      }
    };
  }
  @Override
  public AttachmentDownloadBuilder attachmentDownload() {
    return new AttachmentDownloadBuilder() {
      @Override
      public AttachmentDownloadBuilder userName(String userName) {
        return this;
      }
      @Override
      public AttachmentDownloadBuilder userId(String userId) {
        return this;
      }
      @Override
      public AttachmentDownloadBuilder processId(String processId) {
        return this;
      }
      @Override
      public AttachmentDownloadBuilder attachmentId(String attachmentId) {
        return this;
      }
      @Override
      public Uni<AttachmentDownloadUrl> build() {
        return Uni.createFrom().nullItem();
      }
    };
  }
  @Override
  public AttachmentQuery queryAttachments() {
    return new AttachmentQuery() {
      @Override
      public Uni<List<Attachment>> taskId(String taskId, String processId) {
        return Uni.createFrom().item(Collections.emptyList());
      }
      @Override
      public Uni<List<Attachment>> processId(String processId) {
        return Uni.createFrom().item(Collections.emptyList());
      }
    };
  }
  @Override
  public AuthorizationActionQuery authorizationActionQuery() {
    return new AuthorizationActionQuery() {
      private final List<String> userRoles = new ArrayList<>();
      @Override
      public AuthorizationActionQuery userRoles(List<String> userRoles) {
        this.userRoles.addAll(userRoles);
        return this;
      }
      @Override
      public Uni<AuthorizationAction> get() {
        return Uni.createFrom().item(ImmutableAuthorizationAction.builder()
            .addAllUserRoles(userRoles)
            .build());
      }
    };
  }
}
