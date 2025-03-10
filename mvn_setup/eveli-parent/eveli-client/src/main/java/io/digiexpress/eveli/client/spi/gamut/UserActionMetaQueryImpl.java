package io.digiexpress.eveli.client.spi.gamut;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import io.digiexpress.eveli.client.api.GamutClient.UserActionMeta;
import io.digiexpress.eveli.client.api.GamutClient.UserActionMetaQuery;
import io.digiexpress.eveli.client.api.GamutClient.WorkflowNotFoundException;
import io.digiexpress.eveli.client.api.ImmutableUserActionMeta;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class UserActionMetaQueryImpl implements UserActionMetaQuery {
  private final EveliEnvirClient envir;
  
  private String locale;
  private String actionId;
  
  @Override
  public Uni<UserActionMeta> getOne() {
    TaskAssert.notNull(actionId, () -> "actionId can't be null!");
    TaskAssert.notNull(locale, () -> "locale can't be null!");
    
    return envir.runtimeQuery().getOne().onItem().transform(runtime -> getOne(runtime));

  }
  
  
  public UserActionMeta getOne(EveliRuntime runtime) {
    TaskAssert.notNull(actionId, () -> "actionId can't be null!");
    TaskAssert.notNull(locale, () -> "locale can't be null!");
    
    final var now = OffsetDateTime.now();
    final var stencilSite = runtime.getStencil(now);
    final var stencilService = stencilSite.getSites().get(locale).getLinks().get(actionId);

    if(stencilService == null) {
      throw new WorkflowNotFoundException(new StringBuilder()
          .append("Can't find stencil service by id: '").append(actionId).append("'!")
          .toString());
    }

    final var expiresInSeconds = stencilService.getEndDate() == null ? null : ChronoUnit.SECONDS
        .between(Instant.now().atOffset(now.getOffset()).toLocalDateTime(), stencilService.getEndDate());
    if(expiresInSeconds != null && expiresInSeconds <= 0) {
      throw new WorkflowNotFoundException(new StringBuilder()
          .append("Can't find stencil service by id: '").append(actionId).append("'!")
          .toString());
    }
    
    return ImmutableUserActionMeta.builder()
        .actionId(actionId)
        .expiresInSeconds(expiresInSeconds)
        .topicLink(stencilService)
        .build();
  }
}
