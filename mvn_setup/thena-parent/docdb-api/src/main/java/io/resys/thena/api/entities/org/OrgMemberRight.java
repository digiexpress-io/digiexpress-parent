package io.resys.thena.api.entities.org;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;

@Value.Immutable
public
interface OrgMemberRight extends ThenaOrgObject, IsOrgObject {
  String getId();
  String getCommitId();
  String getMemberId();
  String getRightId();
  @Nullable String getPartyId();
  
  @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgUserRole; };
}