package io.resys.thena.docdb.api.models;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.docdb.api.models.ThenaEnvelope.ThenaObjects;
import io.vertx.core.json.JsonObject;

public interface ThenaOrgObject {
  interface IsOrgObject { String getId(); OrgDocType getDocType(); }
  interface IsOrgVersionObject { String getCommitId(); }  

  @Value.Immutable
  interface OrgGroupAndRoleFlattened extends ThenaOrgObject {
  	String getGroupId();
  	String getGroupName();
  	String getGroupDescription();
  	@Nullable String getGroupParentId();
  	@Nullable String getMembershipId();
  	
  	@Nullable String getGroupStatusId();
  	@Nullable OrgActorStatusType getGroupStatus();
  	@Nullable String getGroupStatusUserId();

  	@Nullable String getRoleName();
  	@Nullable String getRoleDescription();
    
    @Nullable String getRoleId();
    @Nullable String getRoleStatusId();
    @Nullable OrgActorStatusType getRoleStatus();    
  }

  @Value.Immutable
  interface OrgRoleFlattened extends ThenaOrgObject {
    String getRoleId();
    String getRoleName();
    String getRoleDescription();
    
    @Nullable String getRoleStatusId();
    @Nullable OrgActorStatusType getRoleStatus();
  }
  
  @Value.Immutable
  interface OrgUserFlattened extends ThenaOrgObject {
    String getId();
    String getCommitId();
    @Nullable String getExternalId();
    String getUserName();
    String getEmail();
    
    @Nullable String getStatusId();
    @Nullable OrgActorStatusType getStatus();
  }

  
  @Value.Immutable
  interface OrgGroup extends ThenaOrgObject, IsOrgObject, IsOrgVersionObject {
    String getId();
    String getCommitId();

    @Nullable String getExternalId();
    @Nullable String getParentId();
    String getGroupName(); 
    String getGroupDescription();
    
    @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgGroup; };
  }
  
  @Value.Immutable
  interface OrgRole extends ThenaOrgObject, IsOrgObject, IsOrgVersionObject {
    String getId();
    String getCommitId();
    @Nullable String getExternalId();
    String getRoleName();
    String getRoleDescription();
    
    @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgRole; };
  }
  

  @Value.Immutable
  interface OrgUser extends ThenaOrgObject, ThenaObjects, IsOrgObject, IsOrgVersionObject {
    String getId();
    String getCommitId();
    @Nullable String getExternalId();
    String getUserName();
    String getEmail();
    
    @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgUser; };
  }
  

  @Value.Immutable
  interface OrgUserMembership extends ThenaOrgObject, IsOrgObject {
    String getId();
    String getUserId();
    String getGroupId();
    String getCommitId();
    
    @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgUserMembership; };
  }
  
  
  @Value.Immutable
  interface OrgGroupRole extends ThenaOrgObject, IsOrgObject {
    String getId();
    String getCommitId();
    String getGroupId();
    String getRoleId();
    
    @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgGroupRole; };
  }
  
  @Value.Immutable
  interface OrgUserRole extends ThenaOrgObject, IsOrgObject {
    String getId();
    String getCommitId();
    String getUserId();
    String getRoleId();
    
    @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgUserRole; };
  }
  
  
  @Value.Immutable
  interface OrgActorStatus extends ThenaOrgObject, IsOrgObject, IsOrgVersionObject {
    String getId();
    String getCommitId();
    @Nullable String getUserId();
    @Nullable String getRoleId();
    @Nullable String getGroupId();
    OrgActorStatusType getValue();
    @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgActorStatus; };
  }
  
  @Value.Immutable
  interface OrgCommit extends ThenaOrgObject, IsOrgObject {
    String getId();
    @Nullable String getParentId();
    List<OrgCommitTree> getTree();
    String getAuthor();
    String getMessage();
    String getLog();
    OffsetDateTime getCreatedAt();
    @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgCommit; };
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
    
    @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgCommitTree; };
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
    @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgActorData; };
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
  
  enum OrgActorStatusType {
    IN_FORCE, DISABLED
  }
  
  enum OrgUserRoleOrGroupType {
    ROLE, GROUP
  }
  
  enum OrgDocType {
    OrgActorData, 
    OrgActorStatus,
    OrgUserRole,
    OrgGroupRole,
    OrgUserMembership,
    OrgUser,
    OrgRole,
    OrgGroup,
    OrgCommit,
    OrgCommitTree
  }
}
