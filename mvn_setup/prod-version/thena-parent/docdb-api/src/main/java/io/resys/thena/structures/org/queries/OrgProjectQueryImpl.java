package io.resys.thena.structures.org.queries;

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

import io.resys.thena.api.ThenaClient.OrgProjectQuery;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.org.ImmutableOrgProjectObjects;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgProjectObjects;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.org.OrgQueries;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrgProjectQueryImpl implements OrgProjectQuery {
  private final DbState state;
  private final String repoId;

  @Override
  public Uni<QueryEnvelope<OrgProjectObjects>> get() {
    RepoAssert.notEmpty(repoId, () -> "projectName can't be empty!");
    
    return state.toOrgState(repoId)
    .onItem().transformToUni(orgState -> {
      final Tenant repo = orgState.getDataSource().getTenant();      
      return getProjectObjects(orgState.query())
        .onFailure().recoverWithItem(e -> QueryEnvelope.fatalError(repo, "Failed to fetch the world state", log, e));
    });
  }

  private Uni<QueryEnvelope<OrgProjectObjects>> getProjectObjects(OrgQueries org) {

    return Uni.combine().all().unis(
        org.parties().findAll().collect().asList(),
        org.members().findAll().collect().asList(),
        org.rights().findAll().collect().asList(),
        org.memberships().findAll().collect().asList(),
        org.partyRights().findAll().collect().asList(),
        org.memberRights().findAll().collect().asList()
    ).asTuple().onItem().transform(tuple -> {
      
      final var container = ImmutableOrgProjectObjects.builder();

      // GROUP 1
      tuple.getItem1().forEach(group -> container.putParties(group.getId(), group));
      
      // USER 2
      tuple.getItem2().forEach(user -> container.putMembers(user.getId(), user));
      
      // ROLE 3
      tuple.getItem3().forEach(role -> container.putRights(role.getId(), role));
      
      // MEMBERSHIP 4
      tuple.getItem4().forEach(member -> container.putMemberships(member.getId(), member));
      
      // GROUP_ROLE 5
      tuple.getItem5().forEach(groupRole -> container.putPartyRights(groupRole.getId(), groupRole));
      
      // USER_ROLE 6
      tuple.getItem6().forEach(userRole -> container.putMemberRights(userRole.getId(), userRole));
      
      for(final var mem : tuple.getItem4()) {
        if(tuple.getItem2().stream().filter(e -> e.getId().equals(mem.getMemberId())).findFirst().isEmpty()) {
          break;
        }
      }

      final QueryEnvelope<OrgProjectObjects> envelope = ImmutableQueryEnvelope.<OrgProjectObjects>builder()
          .objects(container.build())
          .repo(org.getDataSource().getTenant())
          .status(QueryEnvelopeStatus.OK)
          .build();
      return envelope;
    });
  }
}
