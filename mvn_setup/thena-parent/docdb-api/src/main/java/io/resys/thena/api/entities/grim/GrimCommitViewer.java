package io.resys.thena.api.entities.grim;

import java.time.OffsetDateTime;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;

@Value.Immutable
public interface GrimCommitViewer extends IsGrimObject, ThenaTable {
  String getId();
  String getCommitId();

  OffsetDateTime getCreatedAt();
  String getObjectId();
  GrimDocType getObjectType();
  String getUsedBy();
  String getUsedFor();
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_COMMIT_VIEWER; };
}