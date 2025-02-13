package io.resys.thena.api.entities.org;

import javax.annotation.Nullable;

import org.immutables.value.Value;

@Value.Immutable
public
interface OrgMemberHierarchyEntry extends ThenaOrgObject {
	String getPartyId();
	String getPartyName();
	String getPartyDescription();
	@Nullable String getPartyParentId();
	@Nullable String getMembershipId();
	@Nullable String getMemberId();
	@Nullable OrgActorStatusType getPartyStatus();
	
  @Nullable String getRightId();
  @Nullable String getRightName();
  @Nullable String getRightDescription();
  @Nullable OrgActorStatusType getRightStatus();    
}