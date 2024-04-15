package io.resys.thena.api.entities.org;

import java.util.Collection;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;
import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgVersionObject;
import io.resys.thena.api.envelope.ThenaContainer;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;

@Value.Immutable
public interface OrgMember extends ThenaOrgObject, ThenaContainer, IsOrgObject, IsOrgVersionObject, ThenaTable {
  String getId();
  String getCommitId();
  String getCreatedWithCommitId();
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