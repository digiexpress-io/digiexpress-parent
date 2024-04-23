package io.resys.thena.api.entities.doc;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.entities.doc.DocEntity.IsDocObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;

@Value.Immutable
public interface DocCommit extends DocEntity, IsDocObject, ThenaTable {
  String getId();
  String getDocId();
  Optional<String> getParent();
  
  Optional<String> getBranchId();

  OffsetDateTime getCreatedAt();
  String getCommitAuthor();
  String getCommitLog();
  String getCommitMessage();
}