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

import java.util.function.Supplier;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.ImmutableAttachmentDownloadUrl;
import io.thestencil.iam.api.UserActionsClient.Attachment;
import io.thestencil.iam.api.UserActionsClient.AttachmentDownloadBuilder;
import io.thestencil.iam.api.UserActionsClient.AttachmentDownloadUrl;
import io.thestencil.iam.api.UserActionsClient.UserActionQuery;
import io.thestencil.iam.api.UserActionsClient.UserActionsClientConfig;
import io.thestencil.iam.spi.support.PortalAssert;
import io.vertx.core.http.RequestOptions;

public class AttachmentDownloadBuilderDefault extends MessagesQueryBuilderDefault implements AttachmentDownloadBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentDownloadBuilderDefault.class);
  
  private final Supplier<UserActionQuery> userAction;
  
  private String processId;
  private String userName;
  private String userId;
  private String attachmentId;
  
  public AttachmentDownloadBuilderDefault(RequestOptions init, UserActionsClientConfig config, Supplier<UserActionQuery> userAction, JsonWebToken idToken) {
    super(init, config, idToken);
    this.userAction = userAction;
  }
  
  @Override
  public AttachmentDownloadBuilder processId(String processId) {
    this.processId = processId;
    return this;
  }

  @Override
  public AttachmentDownloadBuilder userName(String userName) {
    this.userName = userName;
    return this;
  }

  @Override
  public AttachmentDownloadBuilder userId(String userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public AttachmentDownloadBuilder attachmentId(String attachmentId) {
    this.attachmentId = attachmentId;
    return this;
  }

  @Override
  public Uni<AttachmentDownloadUrl> build() {
    PortalAssert.notEmpty(userName, () -> "userName must be defined!");
    PortalAssert.notEmpty(userId, () -> "userId must be defined!");
    PortalAssert.notEmpty(processId, () -> "processId must be defined!");
    PortalAssert.notEmpty(attachmentId, () -> "attachmentId must be defined!");

    return userAction.get().processId(processId).userId(userId).userName(userName).limit(1).list().collect()
        .first().onItem().ifNotNull().transformToUni(action -> {
          final var target = action.getAttachments().stream().filter(attachment -> attachment.getId().equals(attachmentId)).findFirst();
          if(target.isEmpty()) {
            return Uni.createFrom().item(ImmutableAttachmentDownloadUrl.builder()
                .download("no-attachment")
                .build());
          }
          
          return createDownload(target.get());
        });
  }

  private Uni<AttachmentDownloadUrl> createDownload(Attachment attachment) {
    final String uri;
    String fileName;
    try {
      fileName = java.net.URLEncoder.encode(attachment.getName(), "UTF-8").replace("+", "%20");
    } catch(Exception e) {
      fileName = attachment.getName() ;
      LOGGER.error(attachment.getName() + ", failed to encode: " + e.getMessage(), e);
    }
    
    if(attachment.getTaskId() == null) {
      uri = getUri("/attachments/process/" + attachment.getProcessId() + "/files/" + fileName);
    } else {
      uri = getUri("/attachments/task/" + attachment.getTaskId() + "/files/" + fileName);
    }
    
    return get(uri).followRedirects(false).send().onItem().transform(resp -> {
      if (resp.statusCode() != 302) {
        String error = "Attachments download query: Can't create response: uri: '" + uri + "', e = " + resp.statusCode() + " | " + resp.statusMessage() + " | " + resp.headers();
        LOGGER.error(error);
        LOGGER.error("Error body: " + resp.bodyAsString());
        LOGGER.error("Error header: " + resp.bodyAsString());
        return ImmutableAttachmentDownloadUrl.builder()
            .download("not-available")
            .build();
      }

      return ImmutableAttachmentDownloadUrl.builder()
          .download(resp.getHeader("Location"))
          .build();
    });
  }

}
