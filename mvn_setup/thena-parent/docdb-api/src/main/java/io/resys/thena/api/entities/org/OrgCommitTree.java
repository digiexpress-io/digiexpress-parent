package io.resys.thena.api.entities.org;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public
interface OrgCommitTree extends ThenaOrgObject, IsOrgObject {
  enum OrgOperationType {
    ADD, MOD, REM
  }

  String getId();
  String getCommitId();
  @Nullable String getParentCommitId();
  String getActorId();
  String getActorType();
  OrgCommitTree.OrgOperationType getOperationType();
  JsonObject getValue();
  
  @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgCommitTree; };
}