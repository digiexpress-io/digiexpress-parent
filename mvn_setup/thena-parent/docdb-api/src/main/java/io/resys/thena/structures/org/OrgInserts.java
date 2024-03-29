package io.resys.thena.structures.org;

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

import io.resys.thena.api.entities.org.OrgActorData;
import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.api.entities.org.OrgCommit;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.structures.BatchStatus;
import io.smallrye.mutiny.Uni;

public interface OrgInserts {
  
  Uni<OrgBatchForOne> batchMany(OrgBatchForOne output);
  
  @Value.Immutable
  interface OrgBatchForOne {
    OrgCommit getCommit();
    
    List<OrgActorData> getActorData();
    List<OrgActorStatus> getActorStatus();
    List<OrgParty> getParties();
    List<OrgRight> getRights();
    List<OrgMember> getMembers();
    List<OrgPartyRight> getPartyRights();
    List<OrgMemberRight> getMemberRights();
    List<OrgMembership> getMemberships(); 
    List<String> getIdentifiersForUpdates();

    List<OrgMemberRight> getMemberRightsToDelete();
    List<OrgPartyRight> getPartyRightToDelete();
    List<OrgMembership> getMembershipsToDelete();
    List<OrgActorStatus> getStatusToDelete();
    
    
    BatchStatus getStatus();
    String getRepoId();

    Message getLog();
    List<Message> getMessages();
  }
}
