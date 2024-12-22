package io.resys.thena.api.entities.grim;

import java.time.OffsetDateTime;

import org.immutables.value.Value;

import io.resys.thena.api.entities.TenantEntity;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface GrimCommit extends TenantEntity {
  String getCommitId();
  @Nullable String getParentCommitId();  
  @Nullable String getMissionId();
  
  OffsetDateTime getCreatedAt();
  String getCommitAuthor();
  String getCommitLog();
  String getCommitMessage();
}
