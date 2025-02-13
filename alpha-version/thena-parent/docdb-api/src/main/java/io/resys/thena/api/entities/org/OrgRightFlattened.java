package io.resys.thena.api.entities.org;

import javax.annotation.Nullable;

import org.immutables.value.Value;

@Value.Immutable
public
interface OrgRightFlattened extends ThenaOrgObject {
  String getRightId();
  String getRightName();
  String getRightDescription();
  @Nullable OrgActorStatusType getRightStatus();
}