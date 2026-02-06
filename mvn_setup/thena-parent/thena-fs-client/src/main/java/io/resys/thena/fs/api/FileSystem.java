package io.resys.thena.fs.api;

/*-
 * #%L
 * thena-contract-client
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

import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.fs.api.blobs.BlobQuery;
import io.resys.thena.fs.api.branches.BranchBuilder;
import io.resys.thena.fs.api.branches.BranchQuery;
import io.resys.thena.fs.api.commits.CommitBuilder;
import io.resys.thena.fs.api.commits.CommitQuery;
import io.resys.thena.fs.api.tags.TagBuilder;
import io.resys.thena.fs.api.tags.TagQuery;

public interface FileSystem {
  TenantActions tenants();
  
  FileSystemTenant withTenant();
  FileSystemTenant withTenant(String tenantIdOrName);
  FileSystemTenant withTenant(CreatedTenant repo);
  FileSystemTenant withTenant(Tenant repo);

  
  // 
  interface FileSystemTenant {
    // loaded tenant 
    String getTenantId();
    
    CommitBuilder commitBuilder();
    CommitQuery commitQuery();
    
    TagBuilder tagBuilder();
    TagQuery tagQuery();
   
    BranchBuilder branchBuilder();
    BranchQuery branchQuery();
    
    BlobQuery blobQuery();
  }

}
