package io.resys.thena.storesql;

import io.resys.thena.api.entities.ImmutableTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.entities.org.ImmutableOrgActorStatus;
import io.resys.thena.api.entities.org.ImmutableOrgMember;
import io.resys.thena.api.entities.org.ImmutableOrgMemberFlattened;
import io.resys.thena.api.entities.org.ImmutableOrgMemberHierarchyEntry;
import io.resys.thena.api.entities.org.ImmutableOrgMemberRight;
import io.resys.thena.api.entities.org.ImmutableOrgMembership;
import io.resys.thena.api.entities.org.ImmutableOrgParty;
import io.resys.thena.api.entities.org.ImmutableOrgPartyRight;
import io.resys.thena.api.entities.org.ImmutableOrgRight;
import io.resys.thena.api.entities.org.ImmutableOrgRightFlattened;
import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberFlattened;
import io.resys.thena.api.entities.org.OrgMemberHierarchyEntry;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.entities.org.OrgRightFlattened;
import io.resys.thena.datasource.SqlDataMapper;
import io.resys.thena.datasource.TenantTableNames;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SqlMapperImpl implements SqlDataMapper {

  protected final TenantTableNames ctx;
 
  @Override
  public Tenant repo(Row row) {
    return ImmutableTenant.builder()
        .id(row.getString("id"))
        .rev(row.getString("rev"))
        .name(row.getString("name"))
        .externalId(row.getString("external_id"))
        .type(StructureType.valueOf(row.getString("type")))
        .prefix(row.getString("prefix"))
        .build();
  }
  
  @Override
  public JsonObject jsonObject(Row row, String columnName) {
    // string based - new JsonObject(row.getString(columnName));
    return row.getJsonObject(columnName);
  }

 
  @Override
  public OrgMember orgMember(Row row) {
    return ImmutableOrgMember.builder()
        .id(row.getString("id"))
        .externalId(row.getString("external_id"))
        .commitId(row.getString("commit_id"))
        .userName(row.getString("username"))
        .email(row.getString("email"))
        .build();
  }
	@Override
	public OrgParty orgParty(Row row) {
    return ImmutableOrgParty.builder()
        .id(row.getString("id"))
        .externalId(row.getString("external_id"))
        .parentId(row.getString("parent_id"))
        .commitId(row.getString("commit_id"))
        .partyName(row.getString("party_name"))
        .partyDescription(row.getString("party_description"))
        .build();
	}
	@Override
	public OrgRight orgRight(Row row) {
    return ImmutableOrgRight.builder()
        .id(row.getString("id"))
        .externalId(row.getString("external_id"))
        .rightName(row.getString("right_name"))
        .rightDescription(row.getString("right_description"))
        .commitId(row.getString("commit_id"))
        .build();
	}
	@Override
	public OrgMembership orgMembership(Row row) {
    return ImmutableOrgMembership.builder()
        .id(row.getString("id"))
        .commitId(row.getString("commit_id"))
        .memberId(row.getString("member_id"))
        .partyId(row.getString("party_id"))
        .build();
	}
	
	@Override
	public OrgPartyRight orgPartyRright(Row row) {
    return ImmutableOrgPartyRight.builder()
        .id(row.getString("id"))
        .commitId(row.getString("commit_id"))
        .rightId(row.getString("right_id"))
        .partyId(row.getString("party_id"))
        .build();
	}
	
	@Override
	public OrgMemberRight orgMemberRight(Row row) {
    return ImmutableOrgMemberRight.builder()
        .id(row.getString("id"))
        .commitId(row.getString("commit_id"))
        .rightId(row.getString("right_id"))
        .memberId(row.getString("member_id"))
        .partyId(row.getString("party_id"))
        .build();
	}
	@Override
	public OrgMemberHierarchyEntry orgMemberHierarchyEntry(Row row) {
		final var roleStatus = row.getString("right_status");
		final var groupStatus = row.getString("status");
		
		return ImmutableOrgMemberHierarchyEntry.builder()
		  	.partyId(row.getString("id"))
		  	.partyParentId(row.getString("parent_id"))
		  	.partyName(row.getString("party_name"))
        .partyDescription(row.getString("party_description"))
		  	.membershipId(row.getString("membership_id"))
		  	
		  	.partyStatusId(row.getString("status_id"))
		  	.partyStatus(groupStatus != null ? OrgActorStatus.OrgActorStatusType.valueOf(groupStatus) : null)
		  	.partyStatusMemberId(row.getString("status_member_id"))
		  	
		  	.rightId(row.getString("right_id"))
		  	.rightName(row.getString("right_name"))
        .rightDescription(row.getString("right_description"))
        
		  	.rightStatus(roleStatus != null ? OrgActorStatus.OrgActorStatusType.valueOf(roleStatus) : null)
		  	.rightStatusId(row.getString("right_status_id"))
				.build();
	}
	
  @Override
  public OrgRightFlattened orgOrgRightFlattened(Row row) {
    final var roleStatus = row.getString("right_status");
    return ImmutableOrgRightFlattened.builder()
        .rightId(row.getString("right_id"))
        .rightName(row.getString("right_name"))
        .rightDescription(row.getString("right_description"))
        .rightStatus(roleStatus != null ? OrgActorStatus.OrgActorStatusType.valueOf(roleStatus) : null)
        .rightStatusId(row.getString("right_status_id"))
        .build();
  }
  @Override
  public OrgMemberFlattened orgMemberFlattened(Row row) {
    final var userStatus = row.getString("user_status");
    return ImmutableOrgMemberFlattened.builder()
        .id(row.getString("id"))
        .externalId(row.getString("external_id"))
        .commitId(row.getString("commit_id"))
        .userName(row.getString("username"))
        .email(row.getString("email"))
        .status(userStatus != null ? OrgActorStatus.OrgActorStatusType.valueOf(userStatus) : null)
        .statusId(row.getString("user_status_id"))
        .build();
  }
  @Override
  public OrgActorStatus orgActorStatus(Row row) {
    final var actorStatus = row.getString("actor_status");
    return ImmutableOrgActorStatus.builder()
        .id(row.getString("id"))
        .commitId(row.getString("commit_id"))
        .memberId(row.getString("member_id"))
        .rightId(row.getString("right_id"))
        .partyId(row.getString("party_id"))
        .value(actorStatus != null ? OrgActorStatus.OrgActorStatusType.valueOf(actorStatus) : null)
        .build();
  }
  @Override
  public SqlDataMapper withOptions(TenantTableNames options) {
    return new SqlMapperImpl(options);
  }
}
