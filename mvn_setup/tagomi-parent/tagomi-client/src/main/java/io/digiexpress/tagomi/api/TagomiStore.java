package io.digiexpress.tagomi.api;

/*-
 * #%L
 * tagomi-client
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

import java.util.List;

import org.immutables.value.Value;

import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.digiexpress.tagomi.api.entities.TagomiContainer.TagomiDocType;
import io.digiexpress.tagomi.api.entities.TagomiEntityContainer;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;

public interface TagomiStore {
  TagomiStore withTenant(String repoId, String headName);
  BranchQuery queryBranches();
  UpsertBuilder upsertBuilder();
  TenantBuilder tenantBuilder();
  
  StateQuery stateQuery();

  
  interface BranchQuery {
    Uni<List<io.resys.thena.api.entities.git.Branch>> findOneBranch();
    Uni<List<TagomiContainer.Tag>> findAllTags();
  }
  
  interface StateQuery {
    Uni<TagomiEntityContainer> getEntityState(String blobId, TagomiDocType type);
    Uni<TagomiContainer> getState();
    Uni<TagomiContainer> getStateByCommitId(String commitId);
    Uni<TagomiContainer> findAllStateObjectsById(List<String> ids, TagomiContainer.TagomiDocType type);
  }
  
  interface UpsertBuilder {
    <T extends TagomiContainer.IsTagomiObject> Uni<T> delete(T toBeDeleted);
    <T extends TagomiContainer.IsTagomiObject> Uni<T> save(T toBeSaved);
    <T extends TagomiContainer.IsTagomiObject> Uni<T> create(T toBeSaved);
    Uni<List<? extends TagomiContainer.IsTagomiObject>> saveAll(List<TagomiContainer.IsTagomiObject> toBeSaved);
    Uni<TagomiContainer> batch(BatchCommand batch);
  }
  
  
  interface TenantBuilder {
    TenantBuilder tenantName(String repoName);
    TenantBuilder headName(String headName);
    Uni<TagomiStore> create();    
    TagomiStore build();
    Uni<Tuple2<Boolean, TagomiStore>> createIfNot();
  }
  
  @Value.Immutable
  interface BatchCommand {
    List<TagomiContainer.IsTagomiObject> getToBeCreated();
    List<TagomiContainer.IsTagomiObject> getToBeSaved();
    List<TagomiContainer.IsTagomiObject> getToBeDeleted();
  }

}
