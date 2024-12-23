package io.resys.thena.api.entities.doc;

import java.time.OffsetDateTime;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.doc.DocEntity.IsDocObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public interface Doc extends DocEntity, IsDocObject, ThenaTable {
  enum DocStatus { IN_FORCE, ARCHIVED }
  
  String getId();
  String getType();
  DocStatus getStatus();
  String getExternalId();

  String getCommitId();
  String getCreatedWithCommitId();

  OffsetDateTime getCreatedAt(); // transitive from commit
  OffsetDateTime getUpdatedAt(); // transitive from commit
  
  @Nullable String getOwnerId();
  @Nullable String getParentId();
  @Nullable JsonObject getMeta();
  
  @JsonIgnore @Override default public DocType getDocType() { return DocType.DOC; };
 
  @Value.Immutable
  interface DocFilter {
    @Nullable List<String> getDocIds();
    @Nullable String getParentId();
    @Nullable String getOwnerId();
    @Nullable String getDocType();
    
    @Nullable String getBranch();
  }

}