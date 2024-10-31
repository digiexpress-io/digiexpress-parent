package io.digiexpress.eveli.assets.spi.builders;

/*-
 * #%L
 * eveli-assets
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.time.ZonedDateTime;
import java.util.Optional;

import io.digiexpress.eveli.assets.api.EveliAssetClient;
import io.digiexpress.eveli.assets.api.EveliAssetClient.AssetState;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Entity;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Workflow;
import io.digiexpress.eveli.assets.api.EveliAssetComposer;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.WorkflowMutator;
import io.digiexpress.eveli.assets.api.ImmutableEntity;
import io.digiexpress.eveli.assets.api.ImmutableWorkflow;
import io.digiexpress.eveli.assets.spi.exceptions.ConstraintException;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class UpdateBuilderImpl implements EveliAssetComposer.UpdateBuilder {
  private final EveliAssetClient client;
  
  @Override
  public Uni<Entity<Workflow>> workflow(WorkflowMutator changes) {
    final Uni<AssetState> query = client.queryBuilder().head();
    
    // Change the workflow
    return query.onItem().transformToUni(state -> {
      
      
      final var currentState = state.getWorkflows().get(changes.getId());
      final var newState = ImmutableEntity.<Workflow>builder()
          .from(currentState)
          .body(ImmutableWorkflow.builder().from(currentState.getBody())
              
              .name(Optional.ofNullable(changes.getName()).orElse(currentState.getBody().getName()))
              .flowName(Optional.ofNullable(changes.getFlowName()).orElse(currentState.getBody().getFlowName()))
              .formName(Optional.ofNullable(changes.getFormName()).orElse(currentState.getBody().getFormName()))
              .formTag(Optional.ofNullable(changes.getFormTag()).orElse(currentState.getBody().getFormTag()))
              
              .updated(ZonedDateTime.now())
              .build())
          .build();
      
      final var duplicate = state.getWorkflows().values().stream()
          .filter(p -> !p.getId().equals(changes.getId()))
          .filter(p -> p.getBody().getName().equals(changes.getName()))
          .findFirst();
      
      if(duplicate.isPresent()) {
        throw new ConstraintException(newState, "Workflow: '" + newState.getBody().getName() + "' already exists!");
      }
      
      return client.crudBuilder().save(newState);
      
    });
  }

}
