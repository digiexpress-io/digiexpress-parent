package io.resys.thena.docdb.models.org.memberhierarchy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.models.ImmutableOrgMemberHierarchy;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberHierarchyEntry;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRightFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgMemberHierarchy;
import io.resys.thena.docdb.support.RepoAssert;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MemberTreeBuilder {

  private Logger logger;
  
  private OrgMemberFlattened member;
  private List<OrgMemberHierarchyEntry> partyData;
  private List<OrgRightFlattened> rightsData;
  
  public MemberTreeBuilder member(OrgMemberFlattened member) {                  this.member = RepoAssert.notNull(member,        () -> "member can't be empty!"); return this; }
  public MemberTreeBuilder partyData(List<OrgMemberHierarchyEntry> partyData) { this.partyData = RepoAssert.notNull(partyData,  () -> "partyData can't be empty!"); return this; }
  public MemberTreeBuilder rightData(List<OrgRightFlattened> rightsData) {      this.rightsData = RepoAssert.notNull(rightsData,() -> "rightsData can't be null!"); return this; }
  
  
  public OrgMemberHierarchy build() {
    RepoAssert.notNull(member,      () -> "member can't be empty!");
    RepoAssert.notNull(partyData, () -> "partyData can't be empty!");
    RepoAssert.notNull(rightsData,  () -> "rightsData can't be null!");
    initLogger();
    logInputs();
    
    // format data
    final var visitor = new MemberContainerVisitorImpl(rightsData);
    final MemberTreeContainer container = new MemberContainer(partyData).accept(visitor);

    // create end result
    final ImmutableUserGroupsData result = container.accept(new CreateMemberTreeGroups(rightsData, logger, member));
    final String log = container.accept(new CreateMemberTreeGroupsLog(rightsData, member));
    
    
    return ImmutableOrgMemberHierarchy.builder()
      .userId(member.getId())
      .userName(member.getUserName())
      .externalId(member.getExternalId())
      .email(member.getEmail())
      .commitId(member.getCommitId())
      .status(member.getStatus() == null ? OrgActorStatusType.IN_FORCE : member.getStatus())
      .addAllUserRoleStatus(container.getRoleStatus())
      .addAllUserGroupStatus(container.getGroupStatus())
      .roleNames(result.getRoleNames())
      .groupNames(result.getGroupNames())
      .addAllDirectRoleNames(result.getRoleNamesWithDirectMembership())
      .addAllDirectGroupNames(result.getGroupNamesWithDirectMembership())
      .log(log)
      .build();
  }

  private void logInputs() {
    if(logger != null) {    
      logger.debug("org/member_tree_inputs/{}/user_data \r\n{}", member.getUserName(), JsonObject.mapFrom(member).encodePrettily());
      logger.debug("org/member_tree_inputs/{}/role_data \r\n{}", member.getUserName(), new JsonArray(rightsData).encodePrettily());
      logger.debug("org/member_tree_inputs/{}/group_data \r\n{}", member.getUserName(), new JsonArray(partyData).encodePrettily());
    }
  }
  
  private void initLogger() {
    final var pkgLevel = LoggerFactory.getLogger(LogConstants.SHOW_ORG_USER_TREE_CALC);
    if(pkgLevel.isDebugEnabled()) {
      this.logger = pkgLevel;
      return;
    }
    final var userLevel = LoggerFactory.getLogger(LogConstants.SHOW_ORG_USER_TREE_CALC + "_" + member.getUserName());
    if(userLevel.isDebugEnabled()) {
      this.logger = userLevel;
    }
  }
}
