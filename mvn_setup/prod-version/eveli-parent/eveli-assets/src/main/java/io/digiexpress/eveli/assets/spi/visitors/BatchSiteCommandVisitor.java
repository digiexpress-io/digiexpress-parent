package io.digiexpress.eveli.assets.spi.visitors;

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


import java.util.stream.Collectors;

import io.digiexpress.eveli.assets.api.EveliAssetClient;
import io.digiexpress.eveli.assets.api.EveliAssetClient.AssetBatchCommand;
import io.digiexpress.eveli.assets.api.EveliAssetClient.AssetState;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Entity;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Publication;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Workflow;
import io.digiexpress.eveli.assets.api.EveliAssetClient.WorkflowTag;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.AssetBatch;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.CreatePublication;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.CreateWorkflow;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.CreateWorkflowTag;
import io.digiexpress.eveli.assets.spi.builders.CreateBuilderImpl;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.assets.api.ImmutableAssetBatchCommand;
import io.digiexpress.eveli.assets.api.ImmutableAssetState;


public class BatchSiteCommandVisitor {
  private final EveliAssetClient client;
  private final ImmutableAssetState.Builder next;
  private final DialobClient dialobClient;
  
  public BatchSiteCommandVisitor(AssetState start, EveliAssetClient client, DialobClient dialobClient) {
    super();
    this.client = client;
    this.next = ImmutableAssetState.builder().from(start);
    this.dialobClient = dialobClient;
  }

  public AssetBatchCommand visit(AssetBatch command) {
    final var publications = command.getPublications().stream().map(this::visitPublications).collect(Collectors.toList());
    final var workflows = command.getWorkflows().stream().map(this::visitWorkflows).collect(Collectors.toList());
    final var workflowTags = command.getWorkflowTags().stream().map(this::visitWorkflowTags).collect(Collectors.toList());
    
    return ImmutableAssetBatchCommand.builder()
        .addAllToBeCreated(publications)
        .addAllToBeCreated(workflows)
        .addAllToBeCreated(workflowTags)
        .build();
  }
  
  private Entity<Publication> visitPublications(CreatePublication init) {
    final var created = CreateBuilderImpl.publication(init, next.build(), client);
    next.putPublications(created.getId(), created);
    return created;
  }
  private Entity<Workflow> visitWorkflows(CreateWorkflow init) {
    final var created = CreateBuilderImpl.workflow(init, next.build(), client, dialobClient);
    next.putWorkflows(created.getId(), created);
    return created;
  }
  private Entity<WorkflowTag> visitWorkflowTags(CreateWorkflowTag init) {
    final var created = CreateBuilderImpl.workflowTag(init, next.build(), client);
    next.putWorkflowTags(created.getId(), created);
    return created;
  }
}
