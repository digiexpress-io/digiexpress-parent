package io.digiexpress.eveli.client.spi.gamut;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.Optional;

import io.digiexpress.eveli.client.api.GamutClient.RpsBuilder;
import io.digiexpress.eveli.client.api.GamutClient.RpsCommand;
import io.digiexpress.eveli.client.api.GamutClient.WorkflowNotFoundException;
import io.digiexpress.eveli.client.api.TaskClient;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class RpsBuilderImpl implements RpsBuilder {

  private RpsCommand command;
  private final io.resys.limaone.program.Runtime runtime;
  private final TaskClient taskClient;
  
  @Override
  public RpsBuilder command(RpsCommand command) {
    this.command = command;
    return this;
  }

  @Override
  public Uni<Void> create() {
    final var clientLocale = command.getLocale();
    final var actionId = command.getProductId();
    final var wk = runtime.withTenant(Optional.empty())
        .getBundle()
        .queryWorkflows()
        .externalId(actionId)
        .name(actionId.length() > 3 ? actionId.substring(0, actionId.length() - 3) : null)
        .locale(clientLocale).findOne();
    
    if(wk.isEmpty()) {
      throw new WorkflowNotFoundException(new StringBuilder()
        .append("Can't find stencil service for locale: '").append(clientLocale).append("'!")
        .toString());
    }
    
    
    final var ctx = taskClient.unwrap();
    
    final var config = ctx.getConfig();
    final var grim = config.getClient().grim(config.getTenantName());

    
    
    
    return grim.commit().createOneRps()
        .commitAuthor("gamut")
        .commitMessage("form rps")
        .rps(newRps -> newRps
            
            .formName(wk.get().getFormName())
            .formVersion(wk.get().getFormTag())
            .workflowName(wk.get().getName())
            
            
            .locale(clientLocale)
            .rating(command.getRating())
            .externalId(command.getProductId())
            .comment(command.getComment())
            .build())
        .build().onItem().transformToUni(ignore -> Uni.createFrom().voidItem());
  }

}
