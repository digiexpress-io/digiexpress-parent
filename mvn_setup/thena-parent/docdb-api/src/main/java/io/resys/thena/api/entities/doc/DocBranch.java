package io.resys.thena.api.entities.doc;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.api.entities.doc.DocEntity.IsDocObject;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public
interface DocBranch extends DocEntity, IsDocObject {
  String getId();
  String getCommitId();
  String getBranchName();
  String getDocId();
  Doc.DocStatus getStatus();
  @Nullable JsonObject getValue();  // null when json loading is disabled
  @Nullable String getBranchNameDeleted();
}