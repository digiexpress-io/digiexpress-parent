package io.resys.thena.api.entities.doc;

import java.time.LocalDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import io.vertx.core.json.JsonObject;

@Value.Immutable
public
interface DocFlatted extends DocEntity {
  String getExternalId();
  Optional<String> getExternalIdDeleted();
  
  String getDocId();
  String getDocType();
  Optional<String> getDocParentId();
  Doc.DocStatus getDocStatus();
  Optional<JsonObject> getDocMeta();


  String getBranchId();
  String getBranchName();
  Optional<String> getBranchNameDeleted();
  Doc.DocStatus getBranchStatus();
  JsonObject getBranchValue();
  
  String getCommitAuthor();
  LocalDateTime getCommitDateTime();
  String getCommitMessage();
  Optional<String> getCommitParent();
  String getCommitId();
  
  Optional<String> getDocLogId();
  Optional<JsonObject> getDocLogValue();
}