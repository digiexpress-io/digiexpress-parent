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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.OrgQueryActions.MemberHierarchyQuery;
import io.resys.thena.api.entities.org.ImmutableOrgMemberHierarchy;
import io.resys.thena.api.entities.org.ImmutableOrgProjectObjects;
import io.resys.thena.api.entities.org.OrgActorStatusType;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgMemberHierarchy;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgPartyHierarchy;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.ImmutableQueryEnvelopeList;
import io.resys.thena.api.envelope.OrgPartyLogVisitor;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.DocNotFoundException;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.api.envelope.ThenaContainer;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.org.OrgState;
import io.resys.thena.structures.org.anytree.AnyTreeContainerContextImpl;
import io.resys.thena.structures.org.anytree.AnyTreeContainerImpl;
import io.resys.thena.structures.org.anytree.PartyHierarchyContainerVisitor;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class MemberHierarchyQueryImpl implements MemberHierarchyQuery {
  private final DbState state;
  private final String repoId;

	@Override
	public Uni<QueryEnvelope<OrgMemberHierarchy>> get(String userId) {
		RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
		
    return state.toOrgState(repoId).onItem().transformToUni(orgState -> getUser(orgState, userId));
	}
  @Override
  public Uni<QueryEnvelopeList<OrgMemberHierarchy>> findAll() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    
    return this.state.toOrgState(repoId)
    .onItem().transformToUni(state -> {
      
      final Multi<OrgMember> users = state.query().members().findAll();
      final Multi<QueryEnvelope<OrgMemberHierarchy>> userAndGroups = users.onItem().transformToUni(user -> getUser(state, user.getId())).merge();
      return userAndGroups.collect().asList().onItem().transform(this::createUsersResult);
    });
  }
	
	private Uni<QueryEnvelope<OrgMemberHierarchy>> getUser(OrgState org, String userId) {
    return Uni.combine().all().unis(
        org.query().members().getById(userId),
        org.query().memberships().findAllByMemberId(userId).collect().asList(),
        org.query().memberRights().findAllByMemberId(userId).collect().asList(),
        org.query().partyRights().findAll().collect().asList(),
        org.query().parties().findAll().collect().asList(),
        org.query().rights().findAll().collect().asList()
        
      ).asTuple()
      .onItem().transform(tuple -> {
        if(tuple.getItem1() == null) {
          return docNotFound(userId, new DocNotFoundException());
        }
        return createUserResult(tuple.getItem1(), tuple.getItem2(), tuple.getItem3(), tuple.getItem4(), tuple.getItem5(), tuple.getItem6());
      });
	}
	

	private QueryEnvelope<OrgMemberHierarchy> createUserResult(
	    OrgMember member, 
	    List<OrgMembership> memberships,
	    List<OrgMemberRight> memberRights, 
	    List<OrgPartyRight> partyRights, 
	    List<OrgParty> parties,
	    List<OrgRight> rights
  ) {
	  
    final var ctx = new AnyTreeContainerContextImpl(ImmutableOrgProjectObjects.builder()
        .parties(parties.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .memberships(memberships.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .members(member == null ? Collections.emptyMap() : Arrays.asList(member).stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .memberRights(memberRights.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .partyRights(partyRights.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .rights(rights.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .build());
    
    
    final var container = new AnyTreeContainerImpl(ctx);
    final var logger = new StringBuilder();


    final var allPartyNames = new HashSet<String>();
    final var allRightNames = new HashSet<String>();

    for(final var criteria : parties.stream()
        .sorted((a, b) -> a.getPartyName().compareTo(b.getPartyName()))
        .toList()) {
      
      if(criteria.getStatus() != OrgActorStatusType.IN_FORCE) {
        continue;
      }
      
      final var log = container.accept(new OrgPartyLogVisitor(criteria.getId(), false));
      final var visited = container.accept(new PartyHierarchyContainerVisitor(criteria.getId()));
      final OrgPartyHierarchy party = visited.withLog(log);
      
      if(party.getMembers().isEmpty()) {
        continue;
      }
      logger
      .append(log)
      .append(System.lineSeparator())
      .append(System.lineSeparator()); 
      
      allRightNames.addAll(party.getParentRights().stream().map(e -> e.getRightName()).toList());
      allRightNames.addAll(party.getDirectRights().stream().map(e -> e.getRightName()).toList());
      allPartyNames.add(party.getParty().getPartyName());      
    }
	  
    final var directRights = memberRights.stream()
        .map(r -> ctx.getRight(r.getRightId()))
        .filter(r -> r.getStatus() == OrgActorStatusType.IN_FORCE)
        .map(r -> r.getRightName())
        .toList();
    
    allRightNames.addAll(directRights.stream().sorted().toList());
    
    return ImmutableQueryEnvelope
        .<OrgMemberHierarchy>builder()
        .status(QueryEnvelopeStatus.OK)
        .objects(ImmutableOrgMemberHierarchy.builder()
            .member(member)

            .addAllDirectRightNames(directRights.stream().sorted().toList())
            .addAllRightNames(allRightNames.stream().sorted().toList())
            
            .addAllPartyNames(allPartyNames.stream().sorted().toList())
            .addAllDirectPartyNames(memberships.stream()
                .map(m -> ctx.getParty(m.getPartyId()))
                .filter(m -> m.getStatus() == OrgActorStatusType.IN_FORCE)
                .map(party -> party.getPartyName())
                .sorted()
                .toList())
            .log(logger.toString())
            .build())
        .build();

	}
	
	private QueryEnvelopeList<OrgMemberHierarchy> createUsersResult(List<QueryEnvelope<OrgMemberHierarchy>> users) {
	  final var builder = ImmutableQueryEnvelopeList.<OrgMemberHierarchy>builder()
	      .status(QueryEnvelopeStatus.OK);
	  
	  final List<OrgMemberHierarchy> objects = new ArrayList<>();
	  for(final var resp : users) {
	    objects.add(resp.getObjects());
	    builder.addAllMessages(resp.getMessages());
	    
	    if(resp.getStatus() != QueryEnvelopeStatus.OK) {
	      builder.status(resp.getStatus());
	    }
	  }
	  
    return builder.objects(Collections.unmodifiableList(objects)).build();
  }
	
	private <T extends ThenaContainer> QueryEnvelope<T> docNotFound(String id, DocNotFoundException ex) {
	  final var existing = this.state.getDataSource().getTenant();
    final var msg = new StringBuilder()
      .append("Member not found by given id: '").append(id).append("', from repo: '").append(existing.getId()).append("'!")
      .toString();
    return QueryEnvelope.docNotFound(existing, log, msg, ex);
  }
  
}
