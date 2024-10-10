package io.resys.thena.api.entities.grim;

import java.time.OffsetDateTime;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.resys.thena.api.envelope.ThenaContainer;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;

@Value.Immutable
public interface GrimCommitViewer extends IsGrimObject, ThenaTable, ThenaContainer {
  String getId();
  String getCommitId();
  String getMissionId();
  
  OffsetDateTime getCreatedAt();
  OffsetDateTime getUpdatedAt();
  
  String getObjectId();
  GrimDocType getObjectType();
  
  String getUsedBy();
  String getUsedFor();
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_COMMIT_VIEWER; };
}