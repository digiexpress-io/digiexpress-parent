package io.resys.thena.api.entities.org;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;

@Value.Immutable
public
interface OrgMemberRight extends ThenaOrgObject, IsOrgObject, ThenaTable {
  String getId();
  String getCommitId();
  String getMemberId();
  String getRightId();
  @Nullable String getPartyId();
  
  @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgUserRole; };
}