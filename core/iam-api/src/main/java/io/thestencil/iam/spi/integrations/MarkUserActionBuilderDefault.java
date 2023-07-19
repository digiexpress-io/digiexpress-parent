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

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.UserActionsClient.UserActionsClientConfig;
import io.thestencil.iam.api.UserActionsClient.MarkUserActionBuilder;
import io.thestencil.iam.api.UserActionsClient.UserActionQuery;
import io.thestencil.iam.api.UserActionsClient.UserMessage;
import io.thestencil.iam.spi.support.PortalAssert;
import io.vertx.core.http.RequestOptions;


public class MarkUserActionBuilderDefault extends MessagesQueryBuilderDefault implements MarkUserActionBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(MarkUserActionBuilderDefault.class);
  private final Supplier<UserActionQuery> query;
  
  private String userId;
  private String processId;
  private String userName;
  
  public MarkUserActionBuilderDefault(RequestOptions init, UserActionsClientConfig config, Supplier<UserActionQuery> query) {
    super(init, config);
    this.query = query;
  }
  @Override
  public MarkUserActionBuilderDefault userId(String userId) {
    this.userId = userId;
    return this;
  }
  @Override
  public MarkUserActionBuilderDefault processId(String processId) {
    this.processId = processId;
    return this;
  }
  @Override
  public MarkUserActionBuilderDefault userName(String userName) {
    this.userName = userName;
    return this;
  }
  @Override
  public Uni<List<UserMessage>> build() {
    PortalAssert.notEmpty(userName, () -> "userName must be defined!");
    PortalAssert.notEmpty(userId, () -> "userId must be defined!");
    PortalAssert.notEmpty(processId, () -> "processId must be defined!");

    return query.get().processId(processId).userId(userId).userName(userName).limit(1).list().collect()
      .asList().onItem().ifNotNull()
      .transformToUni(list -> {
        if(list.size() == 1) {
          final var action = list.get(0);
          if(action.getTaskId() != null) {
            return super.getTaskCommentsAndMarkThemViewed(action.getTaskId(), userId);
          }
          LOGGER.error("USER ACTIONS MARK VIEWED: User is trying to associate tasks/messages that do not belong to them: " + processId + "!");
        }
        LOGGER.error("USER ACTIONS MARK VIEWED: There are no messages for the process: " + String.join(";", list.stream().map(e -> e.toString()).collect(Collectors.toList())) + "!");
        
        //LOGGER.error("USER ACTIONS MARK VIEWED: There are no messages for the process: " + processId + "!");
        return Uni.createFrom().item(Collections.emptyList());
      });
  }
}
