package io.resys.thena.api.entities.org;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public interface OrgCommitTree extends ThenaOrgObject, ThenaTable {

  String getId();
  String getCommitId();
  OrgOperationType getOperationType();

  String getActorId();
  String getActorType();
  
  @Nullable JsonObject getBodyBefore();
  @Nullable JsonObject getBodyAfter();
  
  enum OrgOperationType { ADD, REMOVE, MERGE }
  
  @JsonIgnore default public OrgDocType getDocType() { return OrgDocType.OrgCommitTree; };
}