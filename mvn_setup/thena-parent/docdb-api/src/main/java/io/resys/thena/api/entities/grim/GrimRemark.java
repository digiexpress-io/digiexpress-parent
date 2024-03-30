package io.resys.thena.api.entities.grim;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface GrimRemark extends IsGrimObject, ThenaTable {
  String getId();
  @Nullable String getParentId();
  String getCommitId();
  String getMissionId();
  
  String getRemarkText();
  @Nullable String getRemarkStatus();
  String getReporterId();
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_REMARK; };
}