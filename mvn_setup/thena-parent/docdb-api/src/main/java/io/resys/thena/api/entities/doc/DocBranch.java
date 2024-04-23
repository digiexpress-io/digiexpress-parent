package io.resys.thena.api.entities.doc;

import java.time.OffsetDateTime;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.doc.DocEntity.IsDocObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public interface DocBranch extends DocEntity, IsDocObject, ThenaTable {
  String getId();
  String getCommitId();
  String getCreatedWithCommitId();
  OffsetDateTime getCreatedAt(); // transitive from commit
  OffsetDateTime getUpdatedAt(); // transitive from commit
  
  String getBranchName();
  String getDocId();
  Doc.DocStatus getStatus();
  
  @Nullable JsonObject getValue();  // null when json loading is disabled
  
  @JsonIgnore @Override default public DocType getDocType() { return DocType.DOC_BRANCH; };
}