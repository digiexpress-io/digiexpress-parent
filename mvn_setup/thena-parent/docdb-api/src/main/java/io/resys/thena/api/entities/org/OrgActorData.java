package io.resys.thena.api.entities.org;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public
interface OrgActorData extends ThenaOrgObject, IsOrgObject, ThenaTable {
  String getId();
  String getCommitId();
  @Nullable String getParentId();
  @Nullable String getMemberId();
  @Nullable String getRightId();
  @Nullable String getPartyId();
  
  String getDataType();
  JsonObject getValue();
  @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgActorData; };
}