package io.resys.thena.docdb.api.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.vertx.core.json.JsonObject;

public interface ThenaOrgObject {
  interface IsOrgObject { String getId(); }
  interface IsOrgVersionObject { String getVersion(); }  
  
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
  
  
  @Value.Immutable
  interface OrgGroup extends ThenaOrgObject, IsOrgObject, IsOrgVersionObject {
    String getId();
    String getVersion();

    @Nullable String getExternalId();
    @Nullable String getParentId();
    String getGroupName(); 
    String getGroupDescription();
    LocalDateTime getCreatedAt();
  }
  
  @Value.Immutable
  interface OrgRole extends ThenaOrgObject, IsOrgObject, IsOrgVersionObject {
    String getId();
    String getVersion();
    @Nullable String getExternalId();
    String getRoleName();
    String getRoleDescription();
    LocalDateTime getCreatedAt();
  }
  

  @Value.Immutable
  interface OrgUser extends ThenaOrgObject, IsOrgObject, IsOrgVersionObject {
    String getId();
    String getVersion();
    String getExternalId();
    String getUserName();
    String getEmail();
    LocalDateTime getCreatedAt();
  }
  

  @Value.Immutable
  interface OrgUserMembership extends ThenaOrgObject, IsOrgObject {
    String getId();
    String getUserId();
    String getGroupId();
    LocalDateTime getCreatedAt();
  }
  
  
  @Value.Immutable
  interface OrgGroupRole extends ThenaOrgObject, IsOrgObject {
    String getId();
    String getGroupId();
    String getRoleId();
    LocalDateTime getCreatedAt();
  }
  
  @Value.Immutable
  interface OrgUserRole extends ThenaOrgObject, IsOrgObject {
    String getId();
    String getUserId();
    String getRoleId();
    LocalDateTime getCreatedAt();
  }
  
  
  @Value.Immutable
  interface OrgActorStatus extends ThenaOrgObject, IsOrgObject, IsOrgVersionObject {
    String getId();
    String getVersion();
    @Nullable String getUserId();
    @Nullable String getRoleId();
    @Nullable String getGroupId();
    OrgActorValue getValue();
  }
  
  @Value.Immutable
  interface OrgActorCommitLog extends ThenaOrgObject, IsOrgObject {
    String getId();
    @Nullable String getUserId();
    @Nullable String getRoleId();
    @Nullable String getGroupId();
    
    String getLogType();
    JsonObject getValue();
    
    String getCommitAuthor();
    LocalDateTime getCommitDateTime();
    String getCommitMessage();
  }
  
  @Value.Immutable
  interface OrgActorData extends ThenaOrgObject, IsOrgObject {
    String getId();
    @Nullable String getParentId();
    @Nullable String getUserId();
    @Nullable String getRoleId();
    @Nullable String getGroupId();
    
    String getDataType();
    JsonObject getValue();
    
    String getCommitAuthor();
    LocalDateTime getCommitDateTime();
    String getCommitMessage();
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
