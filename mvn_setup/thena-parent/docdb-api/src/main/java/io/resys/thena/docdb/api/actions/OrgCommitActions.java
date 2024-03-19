package io.resys.thena.docdb.api.actions;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.Message;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaEnvelope;
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
    
    Uni<OneUserEnvelope> build();
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

    Uni<OneGroupEnvelope> build();
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
        
    Uni<OneRoleEnvelope> build();
  }
  

  interface ModifyOneMember {
    ModifyOneMember repoId(String repoId);
    ModifyOneMember author(String author);
    ModifyOneMember message(String message);
  
    ModifyOneMember userId(String userId);
    ModifyOneMember externalId(@Nullable String externalId);
    ModifyOneMember userName(String userName);
    ModifyOneMember email(String email); 
    
    ModifyOneMember roles(ModType type, List<String> roleIdOrNameOrExternalId); // group.(id OR externalId OR rolename)
    ModifyOneMember groups(ModType type, List<String> groupIdOrNameOrExternalId);
    ModifyOneMember groupsRoles(ModType type, Map<String, List<String>> addUseGroupRoles);
    
    Uni<OneUserEnvelope> build();
  }


  interface ModifyOneParty {
    ModifyOneParty repoId(String repoId);
    ModifyOneParty author(String author);
    ModifyOneParty message(String message);
    
    ModifyOneParty groupId(String groupId);
    ModifyOneParty parentId(@Nullable String parentId);
    ModifyOneParty externalId(@Nullable String externalId);
    ModifyOneParty partyName(String partyName);
    ModifyOneParty partyDescription(String partyDescription);
    
    ModifyOneParty members(ModType type, List<String> userIds);
    ModifyOneParty rights(ModType type, List<String> roleIds);

    Uni<OneGroupEnvelope> build();
  }

  
  interface ModifyOneRight {
    ModifyOneRight repoId(String repoId);
    ModifyOneRight author(String author);
    ModifyOneRight message(String message);
    
    ModifyOneRight rightId(String roleId);
    ModifyOneRight externalId(@Nullable String externalId);
    ModifyOneRight rightName(String roleName);
    ModifyOneRight rightDescription(String roleDescription);
    
    ModifyOneRight members(ModType type, List<String> memberIds);
    ModifyOneRight parties(ModType type, List<String> partyIds);

    Uni<OneRoleEnvelope> build();
  }
  
  
  
  @Value.Immutable
  interface OneRoleEnvelope extends ThenaEnvelope {
    String getRepoId();
    Repo.CommitResultStatus getStatus();
    List<Message> getMessages();

    @Nullable OrgRight getRole();
  }
  
  
  @Value.Immutable
  interface OneUserEnvelope extends ThenaEnvelope {
    String getRepoId();
    Repo.CommitResultStatus getStatus();
    List<Message> getMessages();
    
    @Nullable OrgMember getUser();
  }
  
  @Value.Immutable
  interface OneGroupEnvelope extends ThenaEnvelope {
    String getRepoId();
    Repo.CommitResultStatus getStatus();
    List<Message> getMessages();
    
    @Nullable OrgParty getGroup();
  }
  
  enum ModType {
    ADD, DISABLED // not implemented yet TODO::, DELETE
  }
}
