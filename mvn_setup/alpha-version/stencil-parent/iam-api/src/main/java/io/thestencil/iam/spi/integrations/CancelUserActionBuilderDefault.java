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

import io.netty.handler.codec.http.HttpHeaderNames;
import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.ImmutableUserAction;
import io.thestencil.iam.api.UserActionsClient.CancelUserActionBuilder;
import io.thestencil.iam.api.UserActionsClient.UserAction;
import io.thestencil.iam.api.UserActionsClient.UserActionQuery;
import io.thestencil.iam.api.UserActionsClient.UserActionsClientConfig;
import io.thestencil.iam.spi.support.BuilderTemplate;
import io.thestencil.iam.spi.support.PortalAssert;
import io.vertx.core.http.RequestOptions;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;


@Slf4j
public class CancelUserActionBuilderDefault extends BuilderTemplate implements CancelUserActionBuilder {
  private final UserActionsClientConfig config;
  private final Supplier<UserActionQuery> query;
  private String userId;
  private String userName;
  private String processId;
  

  public CancelUserActionBuilderDefault(RequestOptions init, UserActionsClientConfig config, Supplier<UserActionQuery> query) {
    super(config.getWebClient(), init);
    this.config = config;
    this.query = query;
  }
  @Override
  public CancelUserActionBuilderDefault userId(String userId) {
    this.userId = userId;
    return this;
  }
  @Override
  public CancelUserActionBuilderDefault userName(String userName) {
    this.userName = userName;
    return this;
  }
  @Override
  public CancelUserActionBuilderDefault processId(String processId) {
    this.processId = processId;
    return this;
  }
  @Override
  public Uni<UserAction> build() {
    PortalAssert.notEmpty(userName, () -> "userName must be defined!");
    PortalAssert.notEmpty(userId, () -> "userId must be defined!");
    PortalAssert.notEmpty(processId, () -> "processId must be defined!");

    return query.get().processId(processId).userId(userId).userName(userName).limit(1).list().collect()
        .asList().onItem().ifNotNull()
        .transformToUni(src -> delete(getUri("/processes/" + processId))
            .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
            .send())
        .onItem().transform(resp -> map(resp, config.getFillPath(), config.getReviewPath()));
  }
  
  private static UserAction map(HttpResponse<?> resp, String fillUri, String reviewUri) {
    int code = resp.statusCode();
    if (code < 200 || code >= 300) {
      String error = "USER ACTIONS CANCEL: Can't create response, e = " + resp.statusCode() + " | " + resp.statusMessage() + " | " + resp.headers();
      log.error(error);
      return ImmutableUserAction.builder()
          .messagesUri("")
          .id("").name("").status("")
          .formId("")
          .reviewUri("")
          .viewed(true)
          .formUri(fillUri)
          .formInProgress(false)
          .build();
    }
    return ImmutableUserAction.builder()
        .id("").name("").status("")
        .formId("")
        .reviewUri("")
        .messagesUri("")
        .formUri(fillUri)

        .viewed(true)
        .formInProgress(false)
        .build();
  }
}
