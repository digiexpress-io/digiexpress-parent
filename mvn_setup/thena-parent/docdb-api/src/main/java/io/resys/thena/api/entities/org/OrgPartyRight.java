package io.resys.thena.api.entities.org;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;

@Value.Immutable
public
interface OrgPartyRight extends ThenaOrgObject, IsOrgObject, ThenaTable {
  String getId();
  String getCommitId();
  String getPartyId();
  String getRightId();
  
  @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgGroupRole; };
}