package io.resys.thena.api.entities.org;

import java.time.OffsetDateTime;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;

@Value.Immutable
public
interface OrgCommit extends ThenaOrgObject, IsOrgObject {
  String getId();
  @Nullable String getParentId();
  List<OrgCommitTree> getTree();
  String getAuthor();
  String getMessage();
  String getLog();
  OffsetDateTime getCreatedAt();
  @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgCommit; };
}