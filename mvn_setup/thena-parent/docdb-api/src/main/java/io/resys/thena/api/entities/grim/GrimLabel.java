package io.resys.thena.api.entities.grim;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public interface GrimLabel extends IsGrimObject, ThenaTable {
  String getId();
  String getCommitId();
  
  String getLabelType();
  JsonObject getLabelBody();
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_LABEL; };
}