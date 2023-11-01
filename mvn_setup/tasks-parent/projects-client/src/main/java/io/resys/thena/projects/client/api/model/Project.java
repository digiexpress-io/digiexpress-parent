package io.resys.thena.projects.client.api.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import javax.annotation.Nullable;

/*-
 * #%L
 * thena-tasks-client
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

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableProject.class) @JsonDeserialize(as = ImmutableProject.class)
public interface Project extends Document {
  String getRepoId();
  RepoType getRepoType();
  
  String getTitle();
  String getDescription();
  List<String> getUsers();
  
  Instant getCreated();
  Instant getUpdated();
  @Nullable Instant getArchived();
  List<ProjectTransaction> getTransactions(); 
  @Value.Default default DocumentType getDocumentType() { return DocumentType.PROJECT_META; }


  @Value.Immutable @JsonSerialize(as = ImmutableProjectTransaction.class) @JsonDeserialize(as = ImmutableProjectTransaction.class)
  interface ProjectTransaction extends Serializable {
    String getId();
    List<ProjectCommand> getCommands(); 
  }

  
  enum RepoType { WRENCH, STENCIL, TASKS, DIALOB }
}
