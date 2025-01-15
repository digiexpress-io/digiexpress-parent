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
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

import io.digiexpress.eveli.assets.api.EveliAssetClient.Workflow;
import io.digiexpress.eveli.assets.api.EveliAssetClient.WorkflowTag;
import io.digiexpress.eveli.client.api.GamutClient.UserActionMeta;
import io.digiexpress.eveli.client.api.GamutClient.UserActionMetaQuery;
import io.digiexpress.eveli.client.api.GamutClient.WorkflowNotFoundException;
import io.digiexpress.eveli.client.api.ImmutableUserActionMeta;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.thestencil.client.api.MigrationBuilder.Sites;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class UserActionMetaQueryImpl implements UserActionMetaQuery {
  private final Supplier<Sites> siteEnvir;
  private final Supplier<ProgramEnvir> programEnvir;
  private final Supplier<WorkflowTag> workflowEnvir;
  private final ZoneOffset offset;
  
  private String locale;
  private String actionId;
  
  @Override
  public UserActionMeta getOne() {
    TaskAssert.notNull(actionId, () -> "actionId can't be null!");
    TaskAssert.notNull(locale, () -> "locale can't be null!");
    
    final var stencilSite = siteEnvir.get();
    final var stencilService = stencilSite.getSites().get(locale).getLinks().get(actionId);

    if(stencilService == null) {
      throw new WorkflowNotFoundException(new StringBuilder()
          .append("Can't find stencil service by id: '").append(actionId).append("'!")
          .toString());
    }

    final var expiresInSeconds = stencilService.getEndDate() == null ? null : ChronoUnit.SECONDS.between(Instant.now().atOffset(offset).toLocalDateTime(), stencilService.getEndDate());
    if(expiresInSeconds != null && expiresInSeconds <= 0) {
      throw new WorkflowNotFoundException(new StringBuilder()
          .append("Can't find stencil service by id: '").append(actionId).append("'!")
          .toString());
    }
    
    final var wkEnvir = workflowEnvir.get();
    final Workflow workflow = wkEnvir.getEntries().stream()
        .filter(w -> w.getName().equals(stencilService.getValue()))
        .findFirst()
        .orElseThrow(() -> new WorkflowNotFoundException(new StringBuilder()
        .append("Can't find workflow by name: '").append(locale).append("'!")
        .toString()));
    
    return ImmutableUserActionMeta.builder()
        .actionId(actionId)
        .workflow(workflow)
        .expiresInSeconds(expiresInSeconds)
        .topicLink(stencilService)
        
        .stencilTagName(stencilSite.getTagName())
        .workflowTagName(wkEnvir.getName())
        
        .build();
  }
}
