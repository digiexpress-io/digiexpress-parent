package io.resys.thena.api.entities.org;

import java.util.Collection;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;
import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgVersionObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;

@Value.Immutable
public interface OrgParty extends ThenaOrgObject, IsOrgObject, IsOrgVersionObject, ThenaTable {
  String getId();
  String getCommitId();
  String getCreatedWithCommitId();
  
  @Nullable String getExternalId();
  @Nullable String getParentId();
  
  OrgDocSubType getPartySubType();
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