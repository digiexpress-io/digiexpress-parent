package io.resys.thena.api.actions;

/*-
 * #%L
 * thena-docdb-api
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

import java.util.List;

import io.resys.thena.api.envelope.DocContainer.DocObject;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.smallrye.mutiny.Uni;

public interface DocQueryActions {
  DocObjectsQuery docQuery();

  enum IncludeInQuery {
    ALL,
    
    COMMANDS,
    COMMITS,
    COMMIT_TREE
  } 
  
  interface DocObjectsQuery {
    DocObjectsQuery branchName(String branchName);
    DocObjectsQuery subStatus(String subStatus);
    DocObjectsQuery docType(String docType);
    DocObjectsQuery parentId(String parentId);
    DocObjectsQuery ownerId(String ownerId);
    DocObjectsQuery include(IncludeInQuery ... includeChildren); // include additional objects on top of "doc and branch"
    DocObjectsQuery emptyBranchBody(); // load the branch with empty body = {} 
    
    Uni<QueryEnvelope<DocObject>> get();
    Uni<QueryEnvelope<DocObject>> findOne(String idOrExternalIdOrName);
    Uni<QueryEnvelope<DocObject>> findOne();
    Uni<QueryEnvelope<DocObject>> get(String idOrExternalIdOrName);
    Uni<QueryEnvelope<DocTenantObjects>> findAll(List<String> idOrExternalIdOrName);
    Uni<QueryEnvelope<DocTenantObjects>> findAll();
  }
}
