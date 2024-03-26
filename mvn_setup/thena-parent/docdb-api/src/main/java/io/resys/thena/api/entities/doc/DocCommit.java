package io.resys.thena.api.entities.doc;

import java.time.LocalDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.entities.doc.DocEntity.IsDocObject;

@Value.Immutable
public
interface DocCommit extends DocEntity, IsDocObject {
  String getId();
  String getBranchId();
  String getDocId();
  String getAuthor();
  LocalDateTime getDateTime();
  String getMessage();
  Optional<String> getParent();    


}