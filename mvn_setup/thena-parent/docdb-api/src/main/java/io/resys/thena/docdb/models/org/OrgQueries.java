package io.resys.thena.docdb.models.org;

import java.util.Collection;
import java.util.List;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgCommit;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberHierarchyEntry;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRightFlattened;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface OrgQueries {
  
  PartyQuery parties();
  MembershipQuery memberships();
  RightsQuery rights();
  MemberQuery members();
  PartyRightsQuery partyRights();
  MemberRightsQuery memberRights();
  
  ActorStatusQuery actorStatus();
  CommitQuery commits();
  
  interface ActorStatusQuery {
    Uni<OrgActorStatus> getById(String id);
    Multi<OrgActorStatus> findAll();

  }
  interface MemberRightsQuery {
    Uni<OrgMemberRight> getById(String id);
    Multi<OrgMemberRight> findAll();
    Multi<OrgMemberRight> findAll(List<String> id);
    Multi<OrgMemberRight> findAllByMemberId(String id);
    Multi<OrgMemberRight> findAllByRightId(String id);
  }
    
  interface PartyRightsQuery {
    Uni<OrgPartyRight> getById(String id);
    Multi<OrgPartyRight> findAll();
    Multi<OrgPartyRight> findAll(List<String> id);
    Multi<OrgPartyRight> findAllByPartyId(String id);
    Multi<OrgPartyRight> findAllByRightId(String id);
  }
  
  
  interface MembershipQuery {
    Multi<OrgMembership> findAll();
    Multi<OrgMembership> findAll(List<String> id);
    Multi<OrgMembership> findAllByPartyId(String id);
    Multi<OrgMembership> findAllByMemberId(String id);
    Uni<OrgMembership> getById(String id);
  }
  
  interface CommitQuery {
    Multi<OrgCommit> findAll();
    Uni<OrgCommit> getById(String id);
  }
  
  
  interface MemberQuery {
    Multi<OrgMember> findAll();
    Multi<OrgMember> findAll(List<String> id);
    Uni<OrgMember> getById(String id); //user.id or user.email or user.external_id 
    Uni<List<OrgMemberHierarchyEntry>> findAllMemberHierarchyEntries(String memberId);
    Uni<List<OrgRightFlattened>> findAllRightsByMemberId(String memberId);
    Uni<OrgMemberFlattened> getStatusById(String memberId);
  }
  
  interface RightsQuery {    
    Multi<OrgRight> findAll();
    Multi<OrgRight> findAll(Collection<String> id);
    Uni<OrgRight> getById(String id);
  }
  
  interface PartyQuery {
    Multi<OrgParty> findAll();
    Multi<OrgParty> findAll(Collection<String> id);
    Uni<OrgParty> getById(String id);
  }
}
