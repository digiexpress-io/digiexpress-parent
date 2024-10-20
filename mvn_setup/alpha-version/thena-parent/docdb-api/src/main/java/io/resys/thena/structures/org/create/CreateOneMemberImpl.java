package io.resys.thena.structures.org.create;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import io.resys.thena.api.actions.ImmutableOneMemberEnvelope;
import io.resys.thena.api.actions.OrgCommitActions.CreateOneMember;
import io.resys.thena.api.actions.OrgCommitActions.OneMemberEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgRight;
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
public class CreateOneMemberImpl implements CreateOneMember {

  private final DbState state;
  private final String repoId;
  
  private String author;
  private String message;

  private String userName;
  private String email;
  private String externalId;

  private Collection<String> addUserToGroups = new LinkedHashSet<>();
  private Collection<String> addUserToRoles = new LinkedHashSet<>();
  private Map<String, List<String>> addUserToGroupRoles = new LinkedHashMap<>();

  @Override public CreateOneMemberImpl author(String author) {         this.author = RepoAssert.notEmpty(author,           () -> "author can't be empty!"); return this; }
  @Override public CreateOneMemberImpl message(String message) {       this.message = RepoAssert.notEmpty(message,         () -> "message can't be empty!"); return this; }
  @Override public CreateOneMemberImpl userName(String userName) {     this.userName = RepoAssert.notEmpty(userName,       () -> "userName can't be empty!"); return this; }
  @Override public CreateOneMemberImpl email(String email) {           this.email = RepoAssert.notEmpty(email,             () -> "email can't be empty!"); return this; }
  
  @Override public CreateOneMemberImpl externalId(String externalId) { this.externalId = externalId; return this; }
  @Override public CreateOneMemberImpl addMemberToParties(String ... addUserToGroups) { this.addUserToGroups.addAll(Arrays.asList(addUserToGroups)); return this; }
  @Override public CreateOneMemberImpl addMemberToParties(List<String> addUserToGroups) { this.addUserToGroups.addAll(RepoAssert.notNull(addUserToGroups, () -> "addUserToGroups can't be empty!")); return this; }
  @Override public CreateOneMemberImpl addMemberRight(List<String> addUserToRoles) { this.addUserToRoles.addAll(RepoAssert.notNull(addUserToRoles, () -> "addUserToRoles can't be empty!")); return this; }
  
  @Override public CreateOneMember addMemberToPartyRight(String partyId, List<String> rightId) {
    RepoAssert.notEmpty(partyId, () -> "partyId can't be empty!");
    RepoAssert.notEmpty(rightId, () -> "rightId can't be empty!");
    RepoAssert.isTrue(!addUserToGroupRoles.containsKey(partyId), () -> "partyId already defined!");
    addUserToGroupRoles.put(partyId, rightId);
    return this;
  } 
  
  @Override
  public Uni<OneMemberEnvelope> build() {
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(userName, () -> "userName can't be empty!");
    RepoAssert.notEmpty(email, () -> "email can't be empty!");

    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(repoId).build();
    return this.state.withOrgTransaction(scope, this::doInTx);
  }
  
  private Uni<OneMemberEnvelope> doInTx(OrgState tx) {
    final List<String> partyIds = new ArrayList<>();
    partyIds.addAll(addUserToGroups);
    partyIds.addAll(addUserToGroupRoles.keySet());
    
    final List<String> roleIds = new ArrayList<>();
    roleIds.addAll(addUserToRoles);
    roleIds.addAll(addUserToGroupRoles.values().stream().flatMap(e -> e.stream()).toList());
    
		// find groups
		final Uni<List<OrgParty>> groupsUni = partyIds.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) : 
			tx.query().parties().findAll(partyIds.stream().distinct().toList()).collect().asList();
		
		// roles
		final Uni<List<OrgRight>> rolesUni = roleIds.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) :
			tx.query().rights().findAll(roleIds.stream().distinct().toList()).collect().asList();
		
		// join data
		return Uni.combine().all().unis(groupsUni, rolesUni).asTuple()
		  .onItem().transformToUni(tuple -> createUser(
			  tx,
	      tuple.getItem1(), 
	      tuple.getItem2()
		  )
		);
  }

  
  private Uni<OneMemberEnvelope> createUser(
      OrgState tx, 
      List<OrgParty> allGroups, List<OrgRight> allRoles) {
    
    final List<OrgParty> addToGroups = new ArrayList<>();
    final List<OrgRight> addToRoles = new ArrayList<>();
    final Map<OrgParty, List<OrgRight>> groups = new HashMap<>();
    final Map<String, List<OrgRight>> groupsBy = new HashMap<>();
    final Map<String, String> groupMapping = new HashMap<>();
    
    
    for(final var group : allGroups) {
      if(this.addUserToGroups.stream().filter(c -> group.isMatch(c)).findFirst().isPresent()) {
        addToGroups.add(group);
      }
      
      final var addToGroupRole = this.addUserToGroupRoles.keySet().stream().filter(c -> group.isMatch(c)).findFirst();
      if(addToGroupRole.isPresent()) {
        final var roles = new ArrayList<OrgRight>();
        groupsBy.put(group.getId(), roles);
        groups.put(group, roles);
        groupMapping.put(addToGroupRole.get(), group.getId());
      }
    }
    
    // assert groups
    if(addToGroups.size() != this.addUserToGroups.size()) {
      final var found = String.join(", ", addToGroups.stream().map(e -> e.getPartyName()).toList());
      final var expected = String.join(", ", this.addUserToGroups);
      return Uni.createFrom().item(ImmutableOneMemberEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all groups(for membership): \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build());
    }    
    if(groups.size() != this.addUserToGroupRoles.size()) {
      final var found = String.join(", ", groups.keySet().stream().map(e -> e.getPartyName()).toList());
      final var expected = String.join(", ", this.addUserToGroupRoles.keySet());
      return Uni.createFrom().item(ImmutableOneMemberEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all groups(for direct role): \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build());
    }
    
    
    
    for(final var role : allRoles) {
      if(this.addUserToRoles.stream().filter(c -> role.isMatch(c)).findFirst().isPresent()) {
        addToRoles.add(role);
      }

      
      for(final var entry : addUserToGroupRoles.entrySet()) {
        final var addRoleToUserGroup = entry.getValue().stream().filter(c -> role.isMatch(c)).findFirst().isPresent();
        if(addRoleToUserGroup) {
          groupsBy.get(groupMapping.get(entry.getKey())).add(role);
          break;
        }
      }
    }
    
    // assert roles
    if(addToRoles.size() != this.addUserToRoles.size()) {
      final var found = String.join(", ", addToRoles.stream().map(e -> e.getRightName()).toList());
      final var expected = String.join(", ", this.addUserToRoles);
      return Uni.createFrom().item(ImmutableOneMemberEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all roles(for user): \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build());
    }    
    if(groups.values().stream().flatMap(e -> e.stream()).count() != this.addUserToGroupRoles.values().stream().flatMap(e -> e.stream()).count()) {
      final var found = String.join(", ", groups.values().stream().flatMap(e -> e.stream()).map(e -> e.getRightName()).toList());
      final var expected = String.join(", ", this.addUserToGroupRoles.values().stream().flatMap(e -> e.stream()).toList());
      return Uni.createFrom().item(ImmutableOneMemberEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all role(for user and group): \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build());
    }

    final OrgBatchForOne batch = new BatchForOneMemberCreate(tx.getDataSource().getTenant().getId(), author, message)
        .externalId(externalId)
        .email(email)
        .addToParty(addToGroups)
        .addToRights(addToRoles)
        .addToPartyWithRights(groups)
        .userName(userName)
        .create(); 

    return tx.insert().batchMany(batch)
      .onItem().transform(rsp -> ImmutableOneMemberEnvelope.builder()
        .repoId(repoId)
        .member(rsp.getMembers().isEmpty() ? null : rsp.getMembers().get(0))
        .addMessages(ImmutableMessage.builder().text(rsp.getLog()).build())
        .addAllMessages(rsp.getMessages())
        .addAllDirectRights(addToRoles)
        .addAllDirectParties(addToGroups)
        .status(BatchStatus.mapStatus(rsp.getStatus()))
        .build());
  }
}
