package io.resys.thena.api.entities.grim;

import org.immutables.value.Value;

import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;

@Value.Immutable
public interface GrimAnyObject {
  String getId();
  String getCommitId();
  String getMissionId();
  GrimDocType getDocType();
}