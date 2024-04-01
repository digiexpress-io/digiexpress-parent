package io.resys.thena.api.entities.grim;

import java.time.OffsetDateTime;

import org.immutables.value.Value;

import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface GrimCommit extends ThenaTable {
  String getCommitId();
  @Nullable String getParentCommitId();  
  @Nullable String getMissionId();
  @Nullable String getLabelId();
  
  OffsetDateTime getCreatedAt();
  String getCommitAuthor();
  String getCommitLog();
  String getCommitMessage();
}