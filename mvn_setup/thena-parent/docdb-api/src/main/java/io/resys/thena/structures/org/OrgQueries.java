package io.resys.thena.structures.org;

import java.util.Collection;
import java.util.List;

import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.api.entities.org.OrgCommit;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberFlattened;
import io.resys.thena.api.entities.org.OrgMemberHierarchyEntry;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.entities.org.OrgRightFlattened;
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
    Multi<OrgActorStatus> findAllByIdRightId(String id);
    Multi<OrgActorStatus> findAllByIdMemberId(String id);
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
    Multi<OrgMember> findAll(Collection<String> id);
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
