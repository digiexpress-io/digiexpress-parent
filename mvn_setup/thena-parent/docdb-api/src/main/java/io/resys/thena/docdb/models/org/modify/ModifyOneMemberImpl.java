package io.resys.thena.docdb.models.org.modify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.resys.thena.docdb.api.actions.ImmutableOneMemberEnvelope;
import io.resys.thena.docdb.api.actions.OrgCommitActions.ModType;
import io.resys.thena.docdb.api.actions.OrgCommitActions.ModifyOneMember;
import io.resys.thena.docdb.api.actions.OrgCommitActions.OneMemberEnvelope;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRight;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgMemberHierarchy;
import io.resys.thena.docdb.models.org.OrgInserts.OrgBatchForOne;
import io.resys.thena.docdb.models.org.OrgState.OrgRepo;
import io.resys.thena.docdb.models.org.modify.BatchForOneMemberModify.NoChangesException;
import io.resys.thena.docdb.models.org.queries.MemberHierarchyQueryImpl;
import io.resys.thena.docdb.spi.DataMapper;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyOneMemberImpl implements ModifyOneMember {

  private final DbState state;

  private String repoId;
  private String author;
  private String message;

  private String userId;
  private Optional<String> userName;
  private Optional<String> email;
  private Optional<String> externalId;

  private Collection<String> allGroups = new LinkedHashSet<>();
  private Collection<String> groupsToAdd = new LinkedHashSet<>();
  private Collection<String> groupsToRemove = new LinkedHashSet<>();
  private Collection<String> allRoles = new LinkedHashSet<>();
  private Collection<String> rolesToAdd = new LinkedHashSet<>();
  private Collection<String> rolesToRemove = new LinkedHashSet<>();
  private Map<String, List<String>> addUseGroupRoles = new HashMap<>();
  private Map<String, List<String>> removeUseGroupRoles = new HashMap<>();
  
  @Override public ModifyOneMemberImpl userId(String userId) {         this.userId = RepoAssert.notEmpty(userId,           () -> "userId can't be empty!"); return this; }
  @Override public ModifyOneMemberImpl repoId(String repoId) {         this.repoId = RepoAssert.notEmpty(repoId,           () -> "repoId can't be empty!"); return this; }
  @Override public ModifyOneMemberImpl author(String author) {         this.author = RepoAssert.notEmpty(author,           () -> "author can't be empty!"); return this; }
  @Override public ModifyOneMemberImpl message(String message) {       this.message = RepoAssert.notEmpty(message,         () -> "message can't be empty!"); return this; }
  @Override public ModifyOneMemberImpl userName(String userName) {     this.userName = Optional.ofNullable(RepoAssert.notEmpty(userName,       () -> "userName can't be empty!")); return this; }
  @Override public ModifyOneMemberImpl email(String email) {           this.email = Optional.ofNullable(RepoAssert.notEmpty(email,             () -> "email can't be empty!")); return this; }
  @Override public ModifyOneMemberImpl externalId(String externalId) { this.externalId = Optional.ofNullable(externalId); return this; }
  @Override
  public ModifyOneMember status(OrgActorStatusType status) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public ModifyOneMember modifyPartyRight(ModType type, String partyIdNameOrExtId, String rightIdNameOrExtId) {
    RepoAssert.notEmpty(partyIdNameOrExtId, () -> "partyIdNameOrExtId can't be empty!");
    RepoAssert.notEmpty(rightIdNameOrExtId, () -> "rightIdNameOrExtId can't be empty!");

    this.allGroups.add(partyIdNameOrExtId);
    this.allRoles.add(rightIdNameOrExtId);
    if(type == ModType.ADD) {
      if(!this.addUseGroupRoles.containsKey(partyIdNameOrExtId)) {
        this.addUseGroupRoles.put(partyIdNameOrExtId, new ArrayList<>());
      }
      this.addUseGroupRoles.get(partyIdNameOrExtId).add(rightIdNameOrExtId);
    } else if(type == ModType.DISABLED) {
      if(!this.removeUseGroupRoles.containsKey(partyIdNameOrExtId)) {
        this.removeUseGroupRoles.put(partyIdNameOrExtId, new ArrayList<>());
      }
      this.removeUseGroupRoles.get(partyIdNameOrExtId).add(rightIdNameOrExtId);
    } else {
      RepoAssert.fail("Unknown modification type: " + type + "!");
    }
    return this; 
  }
  @Override 
  public ModifyOneMemberImpl modifyParties(ModType type, String partyId) { 
    RepoAssert.notEmpty(partyId, () -> "partyId can't be empty!");
    this.allGroups.add(partyId);
    if(type == ModType.ADD) {
      groupsToAdd.add(partyId);
    } else if(type == ModType.DISABLED) {
      groupsToRemove.add(partyId);
    } else {
      RepoAssert.fail("Unknown modification type: " + type + "!");
    }
    return this; 
   }
  @Override 
  public ModifyOneMemberImpl modifyRights(ModType type, String rightId) {
    RepoAssert.notEmpty(rightId, () -> "rightId can't be empty!");
    
    this.allRoles.add(rightId);
    if(type == ModType.ADD) {
      rolesToAdd.add(rightId);
    } else if(type == ModType.DISABLED) {
      rolesToRemove.add(rightId);
    } else {
      RepoAssert.fail("Unknown modification type: " + type + "!");
    }
    return this; 
  }

  @Override
  public Uni<OneMemberEnvelope> build() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(userId, () -> "userId can't be empty!");
    
    RepoAssert.notEmptyIfDefined(userName, () -> "userName can't be empty!");
    RepoAssert.notEmptyIfDefined(email, () -> "email can't be empty!");

    return this.state.toOrgState().withTransaction(repoId, this::doInTx);
  }
  
  
  private Uni<OneMemberEnvelope> doInTx(OrgRepo tx) {
		// find groups
		final Uni<List<OrgParty>> groupsUni = this.allGroups.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) : 
			tx.query().parties().findAll(allGroups).collect().asList();
		
		// roles
		final Uni<List<OrgRight>> rolesUni = this.allRoles.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) :
			tx.query().rights().findAll(allRoles).collect().asList();
		
		final Uni<QueryEnvelope<OrgMemberHierarchy>> userUni = new MemberHierarchyQueryImpl(state).repoId(repoId).get(userId);
	    
		
		// join data
		return Uni.combine().all().unis(userUni, groupsUni, rolesUni).asTuple()
		  .onItem().transformToUni(tuple -> modifyUser(
			  tx,
	      tuple.getItem1(), 
	      tuple.getItem2(),
	      tuple.getItem3()
		  )
		);
  }

  private Uni<OneMemberEnvelope> modifyUser(
      OrgRepo tx, 
      QueryEnvelope<OrgMemberHierarchy> user, 
      List<OrgParty> allGroups, 
      List<OrgRight> allRoles) {
    
    final Map<OrgParty, List<OrgRight>> addUseGroupRoles = new HashMap<>();
    
    final Map<String, List<OrgRight>> addGroupsBy = new HashMap<>();
    final Map<String, String> addGroupMapping = new HashMap<>();
    
    
    if(user.getStatus() == QueryEnvelopeStatus.ERROR) {
      return Uni.createFrom().item(ImmutableOneMemberEnvelope.builder()
          .repoId(repoId)
          .status(Repo.CommitResultStatus.ERROR)
          .addAllMessages(user.getMessages())
          .build());
    }
    
    if(allGroups.size() < this.allGroups.size()) {
      final var found = String.join(", ", allGroups.stream().map(e -> e.getPartyName()).toList());
      final var expected = String.join(", ", this.allGroups);
      return Uni.createFrom().item(ImmutableOneMemberEnvelope.builder()
          .repoId(repoId)
          .status(Repo.CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all groups: \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build());
    }
    
    if(allRoles.size() < this.allRoles.size()) {
      final var found = String.join(", ", allRoles.stream().map(e -> e.getRightName()).toList());
      final var expected = String.join(", ", this.allRoles);
      return Uni.createFrom().item(ImmutableOneMemberEnvelope.builder()
          .repoId(repoId)
          .status(Repo.CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all roles: \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build());
    }
    
    final var modify = new BatchForOneMemberModify(tx.getRepo().getId(), author, message)
      .externalId(externalId)
      .email(email)
      .current(user.getObjects())
      .userName(userName); 

    
    // Remove or add groups 
    allGroups.forEach(group -> {
      if( groupsToAdd.contains(group.getPartyName()) ||
          groupsToAdd.contains(group.getId()) ||
          groupsToAdd.contains(group.getExternalId())
      ) {
        modify.updateParty(ModType.ADD, group);
      }
      
      if( groupsToRemove.contains(group.getPartyName()) ||
          groupsToRemove.contains(group.getId()) ||
          groupsToRemove.contains(group.getExternalId())
       ) {
         modify.updateParty(ModType.DISABLED, group);
       }
      
      
      final var addToGroupRole = this.addUseGroupRoles.keySet().stream().filter(c -> group.isMatch(c)).findFirst();
      if(addToGroupRole.isPresent()) {
        final var roles = new ArrayList<OrgRight>();
        addGroupsBy.put(group.getId(), roles);
        addUseGroupRoles.put(group, roles);
        addGroupMapping.put(addToGroupRole.get(), group.getId());
      }
    });
    
    // Remove or add roles 
    allRoles.forEach(role -> {
      
      if( rolesToAdd.contains(role.getRightName()) ||
          rolesToAdd.contains(role.getId()) ||
          rolesToAdd.contains(role.getExternalId())
      ) {
        modify.updateRoles(ModType.ADD, role);
      }
      
      if( rolesToRemove.contains(role.getRightName()) ||
          rolesToRemove.contains(role.getId()) ||
          rolesToRemove.contains(role.getExternalId())
       ) {
         modify.updateRoles(ModType.DISABLED, role);
       }
      
      
      
      for(final var entry : this.addUseGroupRoles.entrySet()) {
        final var addRoleToUserGroup = entry.getValue().stream().filter(c -> role.isMatch(c)).findFirst().isPresent();
        if(addRoleToUserGroup) {
          addGroupsBy.get(addGroupMapping.get(entry.getKey())).add(role);
        }
      }
      
    });
    
    
    try {
      final OrgBatchForOne batch = modify
          .updateGroupRoles(ModType.ADD, addUseGroupRoles)
          .create();
      return tx.insert().batchMany(batch)
          .onItem().transform(rsp -> ImmutableOneMemberEnvelope.builder()
            .repoId(repoId)
            .member(rsp.getMembers().isEmpty() ? null : rsp.getMembers().get(0))
            .addMessages(rsp.getLog())
            .addAllMessages(rsp.getMessages())
            .status(DataMapper.mapStatus(rsp.getStatus()))
            .build());
    } catch (NoChangesException e) {
      return Uni.createFrom().item(ImmutableOneMemberEnvelope.builder()
            .repoId(repoId)
            .addMessages(ImmutableMessage.builder()
                .exception(e).text("Nothing to commit, data already in the expected state!")
                .build())
            .status(Repo.CommitResultStatus.NO_CHANGES)
            .build());
    }
  }
}
