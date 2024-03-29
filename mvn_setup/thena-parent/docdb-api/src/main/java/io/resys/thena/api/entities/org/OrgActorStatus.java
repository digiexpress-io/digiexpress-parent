package io.resys.thena.api.entities.org;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;
import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgVersionObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;

@Value.Immutable
public
interface OrgActorStatus extends ThenaOrgObject, IsOrgObject, IsOrgVersionObject, ThenaTable {
  enum OrgActorStatusType {
    IN_FORCE, DISABLED
  }
  String getId();
  String getCommitId();
  @Nullable String getMemberId();
  @Nullable String getRightId();
  @Nullable String getPartyId();
  OrgActorStatus.OrgActorStatusType getValue();
  @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgActorStatus; };
}