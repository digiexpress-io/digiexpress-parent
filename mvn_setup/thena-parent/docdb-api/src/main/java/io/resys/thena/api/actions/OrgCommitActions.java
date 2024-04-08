package io.resys.thena.api.actions;

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.api.entities.org.OrgActorStatus.OrgActorStatusType;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgDocSubType;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.smallrye.mutiny.Uni;

public interface OrgCommitActions {
  
  CreateOneMember createOneMember();  
  CreateOneParty createOneParty();
  CreateOneRight createOneRight();
  
  ModifyOneMember modifyOneMember();
  ModifyOneParty modifyOneParty();
  ModifyOneRight modifyOneRight();


  interface CreateOneMember {
    CreateOneMember author(String author);
    CreateOneMember message(String message);
  
    CreateOneMember userName(String userName);
    CreateOneMember email(String email);
    CreateOneMember externalId(@Nullable String externalId);

    CreateOneMember addMemberToParties(String ... groupId);
    CreateOneMember addMemberToParties(List<String> groupId);
    CreateOneMember addMemberRight(List<String> roledId);

    CreateOneMember addMemberToPartyRight(String groupId, List<String> roledId);
    
    Uni<OneMemberEnvelope> build();
  }
  
  interface CreateOneParty {
    CreateOneParty author(String author);
    CreateOneParty message(String message);
   
    CreateOneParty parentId(@Nullable String parentId);
    CreateOneParty externalId(@Nullable String externalId);
    CreateOneParty partyName(String groupName);
    CreateOneParty partyDescription(String groupDescription);
    CreateOneParty partySubType(OrgDocSubType partySubType);
    
    CreateOneParty addMemberToParty(List<String> userId);
    CreateOneParty addRightsToParty(List<String> roledId);

    Uni<OnePartyEnvelope> build();
  }
  
  
  interface CreateOneRight {
    CreateOneRight author(String author);
    CreateOneRight message(String message);

    CreateOneRight externalId(@Nullable String externalId);
    CreateOneRight rightName(String roleName);
    CreateOneRight rightDescription(String roleDescription);
    CreateOneRight rightSubType(OrgDocSubType rightSubType);
    
    CreateOneRight addRightToMembers(List<String> userId);
    CreateOneRight addRightToParties(List<String> groupId);
        
    Uni<OneRightEnvelope> build();
  }
  

  interface ModifyOneMember {
    ModifyOneMember author(String author);
    ModifyOneMember message(String message);
  
    ModifyOneMember memberId(String userId);
    ModifyOneMember externalId(@Nullable String externalId);
    ModifyOneMember userName(String userName);
    ModifyOneMember email(String email);
    ModifyOneMember status(OrgActorStatus.OrgActorStatusType status);
    
    ModifyOneMember modifyRights(ModType type, String rightIdNameOrExtId); // group.(id OR externalId OR rolename)
    ModifyOneMember modifyParties(ModType type, String partyIdNameOrExtId);
    ModifyOneMember modifyPartyRight(ModType type, String partyIdNameOrExtId, String rightIdNameOrExtId);

    Uni<OneMemberEnvelope> build();
  }


  interface ModifyOneParty {
    ModifyOneParty author(String author);
    ModifyOneParty message(String message);
    
    ModifyOneParty partyId(String groupId);
    ModifyOneParty parentId(@Nullable String parentId);
    ModifyOneParty externalId(@Nullable String externalId);
    ModifyOneParty partyName(String partyName);
    ModifyOneParty partyDescription(String partyDescription);
    ModifyOneParty partySubType(OrgDocSubType partySubType);
    
    ModifyOneParty modifyMember(ModType type, String memberIdNameOrExtId);
    ModifyOneParty modifyRight(ModType type, String rightIdNameOrExtId);
    ModifyOneParty modifyMemberRight(ModType type, String memberIdNameOrExtId, String rightIdNameOrExtId);
    ModifyOneParty status(OrgActorStatusType status);    
    Uni<OnePartyEnvelope> build();
  }

  
  interface ModifyOneRight {
    ModifyOneRight author(String author);
    ModifyOneRight message(String message);
    
    ModifyOneRight rightId(String roleId);
    ModifyOneRight externalId(@Nullable String externalId);
    ModifyOneRight rightName(String roleName);
    ModifyOneRight rightDescription(String roleDescription);
    ModifyOneRight rightSubType(OrgDocSubType rightSubType);
    
    ModifyOneRight modifyMember(ModType type, String memberIdNameOrExtId);
    ModifyOneRight modifyParty(ModType type, String partyIdNameOrExtId);
    ModifyOneRight status(OrgActorStatusType status);
    Uni<OneRightEnvelope> build();
  }
  
  
  
  @Value.Immutable
  interface OneRightEnvelope extends ThenaEnvelope {
    String getRepoId();
    CommitResultStatus getStatus();
    List<Message> getMessages();

    // in case of status not ok, nulls
    @Nullable OrgRight getRight();
    @Nullable List<OrgParty> getDirectParties();
    @Nullable List<OrgMember> getDirectMembers();
  }
  
  
  @Value.Immutable
  interface OneMemberEnvelope extends ThenaEnvelope {
    String getRepoId();
    CommitResultStatus getStatus();
    List<Message> getMessages();

    // in case of status not ok, nulls
    @Nullable OrgMember getMember();
    @Nullable List<OrgRight> getDirectRights();
  }
  
  @Value.Immutable
  interface OnePartyEnvelope extends ThenaEnvelope {
    String getRepoId();
    CommitResultStatus getStatus();
    List<Message> getMessages();
    
    // in case of status not ok, nulls
    @Nullable OrgParty getParty();
    @Nullable List<OrgRight> getDirectRights();
    @Nullable List<OrgMember> getDirectMembers();
  }
  
  enum ModType {
    ADD, DISABLED, REMOVE
  }
}
