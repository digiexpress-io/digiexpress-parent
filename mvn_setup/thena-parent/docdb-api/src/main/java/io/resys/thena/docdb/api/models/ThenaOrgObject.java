package io.resys.thena.docdb.api.models;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.vertx.core.json.JsonObject;

public interface ThenaOrgObject {
  interface IsOrgObject { String getId(); }
  interface IsOrgVersionObject { String getVersion(); }  
  
  /*
  @Value.Immutable
  interface OrgUserFlattened extends ThenaOrgObject {
    String getUserId();
    String getExternalId();
    String getUserName();
    String getEmail();
    OrgActorValue getActorStatus();
    @Nullable OrgActorData getActorData();
    Boolean getTransient(); // include roles and groups via parent/child relations of the groups
    List<OrgUserRoleOrGroup> getRoleNames();
    List<OrgUserRoleOrGroup> getGroupNames();  
  }
  
  
  @Value.Immutable
  interface OrgUserRoleOrGroup extends ThenaOrgObject {
    String getId();
    String getNames();
    OrgUserRoleOrGroupType getType();
    OrgActorValue getActorStatus();
  }
  */
  
  @Value.Immutable
  interface OrgGroup extends ThenaOrgObject, IsOrgObject, IsOrgVersionObject {
    String getId();
    String getCommitId();

    @Nullable String getExternalId();
    @Nullable String getParentId();
    String getGroupName(); 
    String getGroupDescription();
  }
  
  @Value.Immutable
  interface OrgRole extends ThenaOrgObject, IsOrgObject, IsOrgVersionObject {
    String getId();
    String getCommitId();
    @Nullable String getExternalId();
    String getRoleName();
    String getRoleDescription();
  }
  

  @Value.Immutable
  interface OrgUser extends ThenaOrgObject, IsOrgObject, IsOrgVersionObject {
    String getId();
    String getCommitId();
    String getExternalId();
    String getUserName();
    String getEmail();
  }
  

  @Value.Immutable
  interface OrgUserMembership extends ThenaOrgObject, IsOrgObject {
    String getId();
    String getUserId();
    String getGroupId();
    String getCommitId();
  }
  
  
  @Value.Immutable
  interface OrgGroupRole extends ThenaOrgObject, IsOrgObject {
    String getId();
    String getCommitId();
    String getGroupId();
    String getRoleId();
  }
  
  @Value.Immutable
  interface OrgUserRole extends ThenaOrgObject, IsOrgObject {
    String getId();
    String getCommitId();
    String getUserId();
    String getRoleId();
  }
  
  
  @Value.Immutable
  interface OrgActorStatus extends ThenaOrgObject, IsOrgObject, IsOrgVersionObject {
    String getId();
    String getCommitId();
    @Nullable String getUserId();
    @Nullable String getRoleId();
    @Nullable String getGroupId();
    
    @Nullable String getUserMembershipId();
    @Nullable String getUserRoleId();
    @Nullable String getGroupRoleId();
    OrgActorValue getValue();
  }
  
  @Value.Immutable
  interface OrgCommit extends ThenaOrgObject, IsOrgObject {
    String getId();
    
    List<OrgCommitTree> getTree();
    String getAuthor();
    String getMessage();
    String getLog();
    Instant getCreatedAt();
  }
  
  
  @Value.Immutable
  interface OrgCommitTree extends ThenaOrgObject, IsOrgObject {
    String getId();
    String getCommitId();
    @Nullable String getParentCommitId();
    String getActorId();
    String getActorType();
    OrgOperationType getOperationType();
    JsonObject getValue();
  }
  
  @Value.Immutable
  interface OrgActorData extends ThenaOrgObject, IsOrgObject {
    String getId();
    String getCommitId();
    @Nullable String getParentId();
    @Nullable String getUserId();
    @Nullable String getRoleId();
    @Nullable String getGroupId();
    
    String getDataType();
    JsonObject getValue();
  }
  
  
  @Value.Immutable  
  interface OrgLock extends ThenaDocObject {
    OrgLockStatus getStatus();
    Optional<OrgGroup> getGroup();
    Optional<OrgRole> getRole();
    Optional<OrgUser> getUser();
    Optional<OrgActorStatus> getActorStatus();
    Optional<String> getMessage();
  }
  
  enum OrgOperationType {
    ADD, MOD
  }
  
  enum OrgLockStatus { 
    LOCK_TAKEN, NOT_FOUND
  }
  
  enum OrgActorValue {
    IN_FORCE, ARCHIVED
  }
  
  enum OrgUserRoleOrGroupType {
    ROLE, GROUP
  }
}
