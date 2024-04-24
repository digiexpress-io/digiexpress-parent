package io.resys.thena.api.entities.doc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.doc.DocEntity.IsDocObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public interface DocCommands extends DocEntity, IsDocObject, ThenaTable {
  String getId();
  String getCommitId();
  
  String getDocId();
  Optional<String> getBranchId();
  OffsetDateTime getCreatedAt(); // transitive from commit
  String getCreatedBy(); // transitive from commit
  
  List<JsonObject> getCommands();
  
  @JsonIgnore @Override default public DocType getDocType() { return DocType.DOC_BRANCH; };
}