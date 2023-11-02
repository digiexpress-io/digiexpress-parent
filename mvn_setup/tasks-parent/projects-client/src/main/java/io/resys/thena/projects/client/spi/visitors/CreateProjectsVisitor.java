package io.resys.thena.projects.client.spi.visitors;

/*-
 * #%L
 * thena-Projects-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.resys.thena.docdb.api.actions.CommitActions.CommitBuilder;
import io.resys.thena.docdb.api.actions.CommitActions.CommitResultEnvelope;
import io.resys.thena.docdb.api.actions.CommitActions.CommitResultStatus;
import io.resys.thena.docdb.api.models.ThenaObject.Commit;
import io.resys.thena.projects.client.api.model.Project;
import io.resys.thena.projects.client.api.model.ProjectCommand.CreateProject;
import io.resys.thena.projects.client.spi.store.DocumentConfig;
import io.resys.thena.projects.client.spi.store.DocumentConfig.DocCommitVisitor;
import io.resys.thena.projects.client.spi.store.DocumentStoreException;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateProjectsVisitor implements DocCommitVisitor<Project> {
  private final List<? extends CreateProject> commands;
  private final List<Project> createdProjects = new ArrayList<Project>();
  
  @Override
  public CommitBuilder start(DocumentConfig config, CommitBuilder builder) {
    
    for(final var command : commands) {
      final var entity = new ProjectCommandVisitor(config).visitTransaction(Arrays.asList(command));
      final var json = JsonObject.mapFrom(entity);
      builder.append(entity.getId(), json);
      createdProjects.add(entity);
    }
    
    return builder.message("Creating Projects");
  }

  @Override
  public Commit visitEnvelope(DocumentConfig config, CommitResultEnvelope envelope) {
    if(envelope.getStatus() == CommitResultStatus.OK) {
      return envelope.getCommit();
    }
    throw new DocumentStoreException("SAVE_FAIL", DocumentStoreException.convertMessages(envelope));
  }

  @Override
  public List<Project> end(DocumentConfig config, Commit commit) {
    return Collections.unmodifiableList(createdProjects);
  }

}