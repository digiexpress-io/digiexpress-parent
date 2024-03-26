package io.resys.thena.api.entities.doc;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.api.entities.doc.DocEntity.IsDocObject;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public
interface Doc extends DocEntity, IsDocObject {
  enum DocStatus {
    IN_FORCE, ARCHIVED
  }
  String getId();
  String getType();
  DocStatus getStatus();
  String getExternalId();
  @Nullable String getOwnerId();
  @Nullable String getExternalIdDeleted();
  @Nullable String getParentId();
  @Nullable JsonObject getMeta();
}