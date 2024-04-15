package io.resys.thena.api.entities.org;

import java.time.OffsetDateTime;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;

@Value.Immutable
public interface OrgCommit extends ThenaOrgObject, ThenaTable {
  String getCommitId();
  @Nullable String getParentId();
  OffsetDateTime getCreatedAt();
  String getCommitAuthor();
  String getCommitLog();
  String getCommitMessage();
  @JsonIgnore default public OrgDocType getDocType() { return OrgDocType.OrgCommit; };
}

