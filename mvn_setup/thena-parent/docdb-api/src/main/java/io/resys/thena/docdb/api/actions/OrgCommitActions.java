package io.resys.thena.docdb.api.actions;

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.Message;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaEnvelope;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRight;
import io.smallrye.mutiny.Uni;

public interface OrgCommitActions {
  
  CreateOneMember createOneMember();  
  CreateOneParty createOneParty();
  CreateOneRight createOneRight();
  
  ModifyOneMember modifyOneMember();
  ModifyOneParty modifyOneParty();
  ModifyOneRight modifyOneRight();


  interface CreateOneMember {
    CreateOneMember repoId(String repoId);
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
    CreateOneParty repoId(String repoId);
    CreateOneParty author(String author);
    CreateOneParty message(String message);
   
    CreateOneParty parentId(@Nullable String parentId);
    CreateOneParty externalId(@Nullable String externalId);
    CreateOneParty partyName(String groupName);
    CreateOneParty partyDescription(String groupDescription);
    
    CreateOneParty addMemberToParty(List<String> userId);
    CreateOneParty addRightsToParty(List<String> roledId);

    Uni<OnePartyEnvelope> build();
  }
  
  
  interface CreateOneRight {
    CreateOneRight repoId(String repoId);
    CreateOneRight author(String author);
    CreateOneRight message(String message);
    
    CreateOneRight externalId(@Nullable String externalId);
    CreateOneRight rightName(String roleName);
    CreateOneRight rightDescription(String roleDescription);

    CreateOneRight addRightToMembers(List<String> userId);
    CreateOneRight addRightToParties(List<String> groupId);
        
    Uni<OneRightEnvelope> build();
  }
  

  interface ModifyOneMember {
    ModifyOneMember repoId(String repoId);
    ModifyOneMember author(String author);
    ModifyOneMember message(String message);
  
    ModifyOneMember memberId(String userId);
    ModifyOneMember externalId(@Nullable String externalId);
    ModifyOneMember userName(String userName);
    ModifyOneMember email(String email);
    ModifyOneMember status(OrgActorStatusType status);
    
    ModifyOneMember modifyRights(ModType type, String rightIdNameOrExtId); // group.(id OR externalId OR rolename)
    ModifyOneMember modifyParties(ModType type, String partyIdNameOrExtId);
    ModifyOneMember modifyPartyRight(ModType type, String partyIdNameOrExtId, String rightIdNameOrExtId);

    Uni<OneMemberEnvelope> build();
  }


  interface ModifyOneParty {
    ModifyOneParty repoId(String repoId);
    ModifyOneParty author(String author);
    ModifyOneParty message(String message);
    
    ModifyOneParty partyId(String groupId);
    ModifyOneParty parentId(@Nullable String parentId);
    ModifyOneParty externalId(@Nullable String externalId);
    ModifyOneParty partyName(String partyName);
    ModifyOneParty partyDescription(String partyDescription);
    
    ModifyOneParty modifyMember(ModType type, String memberIdNameOrExtId);
    ModifyOneParty modifyRight(ModType type, String rightIdNameOrExtId);
    ModifyOneParty modifyMemberRight(ModType type, String memberIdNameOrExtId, String rightIdNameOrExtId);
    ModifyOneParty status(OrgActorStatusType status);
    
    Uni<OnePartyEnvelope> build();
  }

  
  interface ModifyOneRight {
    ModifyOneRight repoId(String repoId);
    ModifyOneRight author(String author);
    ModifyOneRight message(String message);
    
    ModifyOneRight rightId(String roleId);
    ModifyOneRight externalId(@Nullable String externalId);
    ModifyOneRight rightName(String roleName);
    ModifyOneRight rightDescription(String roleDescription);
    
    ModifyOneRight modifyMember(ModType type, String memberIdNameOrExtId);
    ModifyOneRight modifyParty(ModType type, String partyIdNameOrExtId);
    ModifyOneRight status(OrgActorStatusType status);
    
    Uni<OneRightEnvelope> build();
  }
  
  
  
  @Value.Immutable
  interface OneRightEnvelope extends ThenaEnvelope {
    String getRepoId();
    Repo.CommitResultStatus getStatus();
    List<Message> getMessages();

    @Nullable OrgRight getRight();
  }
  
  
  @Value.Immutable
  interface OneMemberEnvelope extends ThenaEnvelope {
    String getRepoId();
    Repo.CommitResultStatus getStatus();
    List<Message> getMessages();
    
    @Nullable OrgMember getMember();
  }
  
  @Value.Immutable
  interface OnePartyEnvelope extends ThenaEnvelope {
    String getRepoId();
    Repo.CommitResultStatus getStatus();
    List<Message> getMessages();
    
    @Nullable OrgParty getParty();
  }
  
  enum ModType {
    ADD, DISABLED, REMOVE
  }
}
