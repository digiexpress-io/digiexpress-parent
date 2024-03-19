package io.resys.thena.docdb.api.actions;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.Message;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaEnvelope;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.smallrye.mutiny.Uni;

public interface OrgCommitActions {
  
  CreateOneUser createOneUser();  
  CreateOneGroup createOneGroup();
  CreateOneRole createOneRole();
  
  ModifyOneUser modifyOneUser();
  ModifyOneGroup modifyOneGroup();
  ModifyOneRole modifyOneRole();


  interface CreateOneUser {
    CreateOneUser repoId(String repoId);
    CreateOneUser author(String author);
    CreateOneUser message(String message);
  
    CreateOneUser userName(String userName);
    CreateOneUser email(String email);
    CreateOneUser externalId(@Nullable String externalId);

    CreateOneUser addUserToGroups(String ... groupId);
    CreateOneUser addUserToGroups(List<String> groupId);
    CreateOneUser addUserToRoles(List<String> roledId);

    CreateOneUser addUserToGroupRoles(String groupId, List<String> roledId);
    
    Uni<OneUserEnvelope> build();
  }
  
  interface CreateOneGroup {
    CreateOneGroup repoId(String repoId);
    CreateOneGroup author(String author);
    CreateOneGroup message(String message);
   
    CreateOneGroup parentId(@Nullable String parentId);
    CreateOneGroup externalId(@Nullable String externalId);
    CreateOneGroup groupName(String groupName);
    CreateOneGroup groupDescription(String groupDescription);
    
    CreateOneGroup addUsersToGroup(List<String> userId);
    CreateOneGroup addRolesToGroup(List<String> roledId);

    Uni<OneGroupEnvelope> build();
  }
  
  
  interface CreateOneRole {
    CreateOneRole repoId(String repoId);
    CreateOneRole author(String author);
    CreateOneRole message(String message);
    
    CreateOneRole externalId(@Nullable String externalId);
    CreateOneRole roleName(String roleName);
    CreateOneRole roleDescription(String roleDescription);

    CreateOneRole addRoleToUsers(List<String> userId);
    CreateOneRole addRoleToGroups(List<String> groupId);
        
    Uni<OneRoleEnvelope> build();
  }
  

  interface ModifyOneUser {
    ModifyOneUser repoId(String repoId);
    ModifyOneUser author(String author);
    ModifyOneUser message(String message);
  
    ModifyOneUser userId(String userId);
    ModifyOneUser externalId(@Nullable String externalId);
    ModifyOneUser userName(String userName);
    ModifyOneUser email(String email); 
    
    ModifyOneUser roles(ModType type, List<String> roleIdOrNameOrExternalId); // group.(id OR externalId OR rolename)
    ModifyOneUser groups(ModType type, List<String> groupIdOrNameOrExternalId);
    ModifyOneUser groupsRoles(ModType type, Map<String, List<String>> addUseGroupRoles);
    
    Uni<OneUserEnvelope> build();
  }


  interface ModifyOneGroup {
    ModifyOneGroup repoId(String repoId);
    ModifyOneGroup author(String author);
    ModifyOneGroup message(String message);
    
    ModifyOneGroup groupId(String groupId);
    ModifyOneGroup parentId(@Nullable String parentId);
    ModifyOneGroup externalId(@Nullable String externalId);
    ModifyOneGroup groupName(String groupName);
    ModifyOneGroup groupDescription(String groupDescription);
    
    ModifyOneGroup users(ModType type, List<String> userIds);
    ModifyOneGroup roles(ModType type, List<String> roleIds);

    Uni<OneGroupEnvelope> build();
  }

  
  interface ModifyOneRole {
    ModifyOneRole repoId(String repoId);
    ModifyOneRole author(String author);
    ModifyOneRole message(String message);
    
    ModifyOneRole roleId(String roleId);
    ModifyOneRole externalId(@Nullable String externalId);
    ModifyOneRole roleName(String roleName);
    ModifyOneRole roleDescription(String roleDescription);
    
    ModifyOneRole users(ModType type, List<String> userIds);
    ModifyOneRole roles(ModType type, List<String> roleIds);

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
