package io.resys.thena.api.entities.org;

import javax.annotation.Nullable;

import org.immutables.value.Value;

@Value.Immutable
public
interface OrgMemberFlattened extends ThenaOrgObject {
  String getId();
  String getCommitId();
  @Nullable String getExternalId();
  String getUserName();
  String getEmail();
  
  @Nullable String getStatusId();
  @Nullable OrgActorStatus.OrgActorStatusType getStatus();
}