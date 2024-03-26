package io.resys.thena.api.entities.org;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;

@Value.Immutable
public
interface OrgMembership extends ThenaOrgObject, IsOrgObject {
  String getId();
  String getMemberId();
  String getPartyId();
  String getCommitId();
  
  @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgUserMembership; };
}