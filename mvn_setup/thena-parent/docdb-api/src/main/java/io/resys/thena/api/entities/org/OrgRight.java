package io.resys.thena.api.entities.org;

import java.util.Collection;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;
import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgVersionObject;

@Value.Immutable
public
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