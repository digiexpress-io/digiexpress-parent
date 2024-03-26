package io.resys.thena.api.entities.doc;

import org.immutables.value.Value;

import io.resys.thena.api.entities.doc.DocEntity.IsDocObject;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public
interface DocLog extends DocEntity, IsDocObject {
  String getId();
  String getBranchId();
  String getDocId();
  String getDocCommitId();
  JsonObject getValue();
}