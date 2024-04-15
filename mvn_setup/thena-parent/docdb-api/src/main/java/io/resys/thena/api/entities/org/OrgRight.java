package io.resys.thena.api.entities.org;

import java.util.Collection;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;
import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgVersionObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;

@Value.Immutable
public interface OrgRight extends ThenaOrgObject, IsOrgObject, IsOrgVersionObject, ThenaTable {
  String getId();
  String getCommitId();
  String getCreatedWithCommitId();
  @Nullable String getExternalId();
  String getRightName();
  String getRightDescription();
  OrgDocSubType getRightSubType();
  
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