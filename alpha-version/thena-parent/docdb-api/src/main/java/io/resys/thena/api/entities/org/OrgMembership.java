package io.resys.thena.api.entities.org;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;

@Value.Immutable
public
interface OrgMembership extends ThenaOrgObject, IsOrgObject, ThenaTable {
  String getId();
  String getMemberId();
  String getPartyId();
  String getCommitId();
  
  @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgUserMembership; };
}