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
	
	@Nullable String getPartyStatusId();
	@Nullable OrgActorStatus.OrgActorStatusType getPartyStatus();
	@Nullable String getPartyStatusMemberId();

	@Nullable String getRightName();
	@Nullable String getRightDescription();
  
  @Nullable String getRightId();
  @Nullable String getRightStatusId();
  @Nullable OrgActorStatus.OrgActorStatusType getRightStatus();    
}