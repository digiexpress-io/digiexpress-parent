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

import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.ImmutableAttachmentDownloadUrl;
import io.thestencil.iam.api.UserActionsClient.*;
import io.thestencil.iam.spi.support.PortalAssert;
import io.vertx.core.http.RequestOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class AttachmentDownloadBuilderDefault extends MessagesQueryBuilderDefault implements AttachmentDownloadBuilder {


  private final Supplier<UserActionQuery> userAction;
  
  private String processId;
  private String userName;
  private String userId;
  private String attachmentId;
  
  public AttachmentDownloadBuilderDefault(RequestOptions init, UserActionsClientConfig config, Supplier<UserActionQuery> userAction) {
    super(init, config);
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
      log.error(attachment.getName() + ", failed to encode: " + e.getMessage(), e);
    }
    
    if(attachment.getTaskId() == null) {
      uri = getUri("/attachments/process/" + attachment.getProcessId() + "/files/" + fileName);
    } else {
      uri = getUri("/attachments/task/" + attachment.getTaskId() + "/files/" + fileName);
    }
    
    return get(uri).followRedirects(false).send().onItem().transform(resp -> {
      if (resp.statusCode() != 302) {
        String error = "Attachments download query: Can't create response: uri: '" + uri + "', e = " + resp.statusCode() + " | " + resp.statusMessage() + " | " + resp.headers();
        log.error(error);
        log.error("Error body: " + resp.bodyAsString());
        log.error("Error header: " + resp.bodyAsString());
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
