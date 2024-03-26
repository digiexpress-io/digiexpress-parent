package io.resys.thena.api.entities.org;

import java.util.Collection;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.ThenaContainer;
import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;
import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgVersionObject;

@Value.Immutable
public
interface OrgMember extends ThenaOrgObject, ThenaContainer, IsOrgObject, IsOrgVersionObject {
  String getId();
  String getCommitId();
  @Nullable String getExternalId();
  String getUserName();
  String getEmail();
  
  @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgUser; };
  
  default boolean isMatch(String IdOrNameOrExtId) {
    return IdOrNameOrExtId.equals(getExternalId()) ||
        IdOrNameOrExtId.equals(getUserName()) ||
        IdOrNameOrExtId.equals(getId());
  }
  default boolean isMatch(Collection<String> IdOrNameOrExtId) {
    return IdOrNameOrExtId.contains(getExternalId()) ||
        IdOrNameOrExtId.contains(getUserName()) ||
        IdOrNameOrExtId.contains(getId());
  }
}