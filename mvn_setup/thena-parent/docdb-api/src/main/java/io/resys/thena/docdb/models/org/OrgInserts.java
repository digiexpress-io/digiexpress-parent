package io.resys.thena.docdb.models.org;

import java.util.List;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import io.resys.thena.docdb.api.models.Message;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorData;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgCommit;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgLock;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserRole;
import io.resys.thena.docdb.models.git.GitInserts.BatchStatus;
import io.smallrye.mutiny.Uni;

public interface OrgInserts {
  
  Uni<OrgBatchForOne> batchOne(OrgBatchForOne output);
  Uni<OrgBatchForMany> batchMany(OrgBatchForMany output);
  
  @Value.Immutable
  interface OrgBatchForOne {
    OrgCommit getCommit();
    
    List<OrgLock> getLock();
    List<OrgActorData> getActorData();
    List<OrgActorStatus> getActorStatus();
    List<OrgGroup> getGroups();
    List<OrgRole> getRoles();
    List<OrgUser> getUsers();
    List<OrgGroupRole> getGroupRoles();
    List<OrgUserRole> getUserRoles();
    List<OrgUserMembership> getUserMemberships(); 
    
    
    BatchStatus getStatus();
    String getRepoId();

    Message getLog();
    List<Message> getMessages();
  }
  
  @Value.Immutable
  interface OrgBatchForMany {
    BatchStatus getStatus();
    Repo getRepo();
    
    List<OrgBatchForOne> getItems();

    Message getLog();
    List<Message> getMessages();
  }
}
