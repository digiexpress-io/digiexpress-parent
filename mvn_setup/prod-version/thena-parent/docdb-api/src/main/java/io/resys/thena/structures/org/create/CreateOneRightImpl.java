package io.resys.thena.structures.org.create;

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
import java.util.Collections;
import java.util.List;

import io.resys.thena.api.actions.ImmutableOneRightEnvelope;
import io.resys.thena.api.actions.OrgCommitActions.CreateOneRight;
import io.resys.thena.api.actions.OrgCommitActions.OneRightEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgDocSubType;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.org.OrgInserts.OrgBatchForOne;
import io.resys.thena.structures.org.OrgState;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateOneRightImpl implements CreateOneRight {

  private final DbState state;

  private final String repoId;
  private String author;
  private String message;

  private String externalId;
  private String roleName;
  private String roleDescription;
  private OrgDocSubType rightSubType;
  private List<String> addToParties = new ArrayList<>();
  private List<String> addToMembers = new ArrayList<>();

  
  @Override public CreateOneRightImpl author(String author) {         this.author = RepoAssert.notEmpty(author,        () -> "author can't be empty!"); return this; }
  @Override public CreateOneRightImpl message(String message) {       this.message = RepoAssert.notEmpty(message,      () -> "message can't be empty!"); return this; }
  @Override public CreateOneRightImpl rightName(String roleName) {    this.roleName = RepoAssert.notEmpty(roleName,  () -> "roleName can't be empty!"); return this; }
  @Override public CreateOneRightImpl rightDescription(String desc) { this.roleDescription = RepoAssert.notEmpty(desc,() -> "roleDescription can't be empty!"); return this; }
  @Override public CreateOneRightImpl externalId(String externalId) { this.externalId = externalId; return this; }
  @Override public CreateOneRightImpl rightSubType(OrgDocSubType rightSubType) { this.rightSubType = rightSubType; return this; }
  
  @Override public CreateOneRightImpl addRightToParties(List<String> addRoleToGroups) { this.addToParties.addAll(RepoAssert.notNull(addRoleToGroups, () -> "addRoleToGroups can't be empty!")); return this; }
  @Override public CreateOneRightImpl addRightToMembers(List<String> addRoleToUsers) { 	 this.addToMembers.addAll(RepoAssert.notNull(addRoleToUsers, () -> "addRoleToUsers can't be empty!")); return this; }
  
  @Override
  public Uni<OneRightEnvelope> build() {
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(roleName, () -> "roleName can't be empty!");
    RepoAssert.notEmpty(roleDescription, () -> "roleDescription can't be empty!");

    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(repoId).build();
    return this.state.withOrgTransaction(scope, this::doInTx);
  }
  
  private Uni<OneRightEnvelope> doInTx(OrgState tx) {
		// find users
		final Uni<List<OrgMember>> usersUni = this.addToMembers.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) : 
			tx.query().members().findAll(addToMembers).collect().asList();
		
		
		// find group
		final Uni<List<OrgParty>> groupsUni = this.addToParties.isEmpty() ?
			Uni.createFrom().item(Collections.emptyList()) : 
			tx.query().parties().findAll(addToParties).collect().asList();
	
		// join data
		return Uni.combine().all().unis(usersUni, groupsUni).asTuple()
		  .onItem().transformToUni(tuple -> createRole(
			  tx,
	      tuple.getItem1(), 
	      tuple.getItem2()
		  )
		);
  }

  private Uni<OneRightEnvelope> createRole(OrgState tx, List<OrgMember> users, List<OrgParty> parties) {
    
    // assert parties
    if(parties.size() != this.addToParties.size()) {
      final var found = String.join(", ", parties.stream().map(e -> e.getPartyName()).toList());
      final var expected = String.join(", ", this.addToParties);
      return Uni.createFrom().item(ImmutableOneRightEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all parties(for right): \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build());
    }    
    
    // assert members
    if(users.size() != this.addToMembers.size()) {
      final var found = String.join(", ", users.stream().map(e -> e.getUserName()).toList());
      final var expected = String.join(", ", this.addToMembers);
      return Uni.createFrom().item(ImmutableOneRightEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all members(for party): \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build());
    }    
    
    
    
    final OrgBatchForOne batch = new BatchForOneRightCreate(tx.getTenantId(), author, message)
        .externalId(externalId)
        .users(users)
        .parties(parties)
        .rightSubType(rightSubType)
        .rightName(roleName)
        .rightDescription(roleDescription)
        .create();

    return tx.insert().batchMany(batch)
      .onItem().transform(rsp -> ImmutableOneRightEnvelope.builder()
        .repoId(repoId)
        .right(rsp.getRights().isEmpty() ? null : rsp.getRights().get(0))
        .addMessages(ImmutableMessage.builder().text(rsp.getLog()).build())
        .addAllMessages(rsp.getMessages())
        .status(BatchStatus.mapStatus(rsp.getStatus()))
        .directParties(parties)
        .directMembers(users)
        .build());
  } 
}
