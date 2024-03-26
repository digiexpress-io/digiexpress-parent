package io.resys.thena.api.entities.org;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.ThenaObjects;
import io.resys.thena.api.entities.doc.ThenaDocObject;
import io.vertx.core.json.JsonObject;

public interface ThenaOrgObject {
  interface IsOrgObject { String getId(); OrgDocType getDocType(); }
  interface IsOrgVersionObject { String getCommitId(); }  

  @Value.Immutable
  interface OrgMemberHierarchyEntry extends ThenaOrgObject {
  	String getPartyId();
  	String getPartyName();
  	String getPartyDescription();
  	@Nullable String getPartyParentId();
  	@Nullable String getMembershipId();
  	
  	@Nullable String getPartyStatusId();
  	@Nullable OrgActorStatusType getPartyStatus();
  	@Nullable String getPartyStatusMemberId();

  	@Nullable String getRightName();
  	@Nullable String getRightDescription();
    
    @Nullable String getRightId();
    @Nullable String getRightStatusId();
    @Nullable OrgActorStatusType getRightStatus();    
  }

  @Value.Immutable
  interface OrgRightFlattened extends ThenaOrgObject {
    String getRightId();
    String getRightName();
    String getRightDescription();
    
    @Nullable String getRightStatusId();
    @Nullable OrgActorStatusType getRightStatus();
  }
  
  @Value.Immutable
  interface OrgMemberFlattened extends ThenaOrgObject {
    String getId();
    String getCommitId();
    @Nullable String getExternalId();
    String getUserName();
    String getEmail();
    
    @Nullable String getStatusId();
    @Nullable OrgActorStatusType getStatus();
  }

  
  @Value.Immutable
  interface OrgParty extends ThenaOrgObject, IsOrgObject, IsOrgVersionObject {
    String getId();
    String getCommitId();

    @Nullable String getExternalId();
    @Nullable String getParentId();
    String getPartyName(); 
    String getPartyDescription();
    
    @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgGroup; };
    
    default boolean isMatch(String idOrNameOrExtId) {
      return idOrNameOrExtId.equals(getExternalId()) ||
          idOrNameOrExtId.equals(getPartyName()) ||
          idOrNameOrExtId.equals(getId());
    }
    default boolean isMatch(Collection<String> idOrNameOrExtId) {
      return idOrNameOrExtId.contains(getExternalId()) ||
          idOrNameOrExtId.contains(getPartyName()) ||
          idOrNameOrExtId.contains(getId());
    }
    
  }
  
  @Value.Immutable
  interface OrgRight extends ThenaOrgObject, IsOrgObject, IsOrgVersionObject {
    String getId();
    String getCommitId();
    @Nullable String getExternalId();
    String getRightName();
    String getRightDescription();
    
    @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgRole; };
    default boolean isMatch(String IdOrNameOrExtId) {
      return IdOrNameOrExtId.equals(getExternalId()) ||
          IdOrNameOrExtId.equals(getRightName()) ||
          IdOrNameOrExtId.equals(getId());
    }
    default boolean isMatch(Collection<String> IdOrNameOrExtId) {
      return IdOrNameOrExtId.contains(getExternalId()) ||
          IdOrNameOrExtId.contains(getRightName()) ||
          IdOrNameOrExtId.contains(getId());
    }
  }
  

  @Value.Immutable
  interface OrgMember extends ThenaOrgObject, ThenaObjects, IsOrgObject, IsOrgVersionObject {
    String getId();
    String getCommitId();
    @Nullable String getExternalId();
    String getUserName();
    String getEmail();
    
    @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgUser; };
  }
  

  @Value.Immutable
  interface OrgMembership extends ThenaOrgObject, IsOrgObject {
    String getId();
    String getMemberId();
    String getPartyId();
    String getCommitId();
    
    @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgUserMembership; };
  }
  
  
  @Value.Immutable
  interface OrgPartyRight extends ThenaOrgObject, IsOrgObject {
    String getId();
    String getCommitId();
    String getPartyId();
    String getRightId();
    
    @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgGroupRole; };
  }
  
  @Value.Immutable
  interface OrgMemberRight extends ThenaOrgObject, IsOrgObject {
    String getId();
    String getCommitId();
    String getMemberId();
    String getRightId();
    @Nullable String getPartyId();
    
    @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgUserRole; };
  }
  
  
  @Value.Immutable
  interface OrgActorStatus extends ThenaOrgObject, IsOrgObject, IsOrgVersionObject {
    String getId();
    String getCommitId();
    @Nullable String getMemberId();
    @Nullable String getRightId();
    @Nullable String getPartyId();
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
    @Nullable String getMemberId();
    @Nullable String getRightId();
    @Nullable String getPartyId();
    
    String getDataType();
    JsonObject getValue();
    @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgActorData; };
  }
  
  
  @Value.Immutable  
  interface OrgLock extends ThenaDocObject {
    OrgLockStatus getStatus();
    Optional<String> getMessage();
  }
  
  
  enum OrgOperationType {
    ADD, MOD, REM
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
