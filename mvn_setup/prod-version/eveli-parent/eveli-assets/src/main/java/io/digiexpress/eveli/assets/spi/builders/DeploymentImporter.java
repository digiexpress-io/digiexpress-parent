package io.digiexpress.eveli.assets.spi.builders;

import java.util.ArrayList;

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

import io.digiexpress.eveli.assets.api.EveliAssetClient;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Entity;
import io.digiexpress.eveli.assets.api.EveliAssetClient.EntityType;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Workflow;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.Deployment;
import io.digiexpress.eveli.assets.api.ImmutableAssetBatchCommand;
import io.digiexpress.eveli.assets.api.ImmutableEntity;
import io.digiexpress.eveli.assets.api.ImmutableWorkflow;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.spi.StencilComposerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class DeploymentImporter {

  private final DialobClient dialob;
  private final HdesClient wrench;
  private final StencilClient stencil;
  private final EveliAssetClient eveliAssets;
  
  public Uni<Void> importData(Deployment deployment) {
    
    final var newWorkflows = new ArrayList<Workflow>();
    deployment.getDialobTag().forEach(form -> {
      //final var exists = dialob.findOneFormById(form.getName()).isPresent();

      final var workflow = deployment.getWorkflowTag().getEntries().stream().filter(e -> e.getFormName().equals(form.getName())).findFirst().get();
      
      final var newForm = dialob.createForm(form);        
      final var newTag = dialob.createTag(newForm.getName(), workflow.getFormTag());

      newWorkflows.add(ImmutableWorkflow.builder()
          .from(workflow)
          .formId(newTag.getFormId())
          .build());  
    });
    
    
    
    final var wrenchImport = new HdesComposerImpl(wrench).importTag(deployment.getWrenchTag());
    final var stencilImport = new StencilComposerImpl(stencil).migration().importData(deployment.getStencilTag());
    
    
    final var workflowImport = eveliAssets.repoBuilder().createIfNot().onItem().transformToUni(junk -> {
      final var workflows = newWorkflows.stream().map(body -> {
        final var gid = eveliAssets.getConfig().getGidProvider().getNextId();
        final Entity<Workflow> entity = ImmutableEntity.<Workflow>builder()
            .id(gid)
            .type(EntityType.WORKFLOW)
            .body(body)
            .build();
        return entity;
      }).toList();
      
      return eveliAssets.crudBuilder().batch(ImmutableAssetBatchCommand.builder()
          .addAllToBeCreated(workflows)
          .build());
    });
    
    
    return Uni.combine().all().unis(wrenchImport, stencilImport, workflowImport).asTuple()
        .onItem().transformToUni(tuple -> {
          
          log.info("imported deployment");
          
          return Uni.createFrom().voidItem();
        });
  }
}
