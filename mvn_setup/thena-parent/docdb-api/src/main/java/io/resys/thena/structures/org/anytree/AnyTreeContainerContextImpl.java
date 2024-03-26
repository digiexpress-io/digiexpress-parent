package io.resys.thena.structures.org.anytree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import io.resys.thena.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.api.models.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.api.models.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.api.models.ThenaOrgObject.OrgRight;
import io.resys.thena.api.models.ThenaOrgObjects.OrgProjectObjects;
import io.resys.thena.api.visitors.OrgTreeContainer.OrgAnyTreeContainerContext;

public class AnyTreeContainerContextImpl implements OrgAnyTreeContainerContext {
  
  private final OrgProjectObjects worldState;
  private final List<OrgParty> groupTops;
  private final List<OrgParty> groupBottoms;
  private final Map<String, List<OrgParty>> groupsByParentId;
  private final Map<String, List<OrgMemberRight>> userRoles;
  private final Map<String, List<OrgMembership>> groupMemberships;
  private final Map<String, List<OrgPartyRight>> groupRoles;
  private final Map<String, List<OrgMembership>> groupInheritedUsers;
  
  private final Map<String, OrgActorStatus> userStatus;
  private final Map<String, OrgActorStatus> groupStatus;
  private final Map<String, OrgActorStatus> roleStatus;
  private final Map<String, OrgActorStatus> groupRoleStatus;
  private final Map<String, OrgActorStatus> userRoleStatus;
  private final Map<String, OrgActorStatus> membershipStatus;
  
  
  public AnyTreeContainerContextImpl(OrgProjectObjects worldState) {
    super();
    this.worldState = worldState;
    
    final var groupTops = new ArrayList<OrgParty>();
    final var groupBottoms = new ArrayList<OrgParty>();
    final var groupsByParentId = new LinkedHashMap<String, List<OrgParty>>();
    final var allGroups = worldState.getParties().values();
    
    for(final var group : allGroups) {
      if(group.getParentId() == null) {
        groupTops.add(group);    
      } 
      
      if(group.getParentId() != null) {
        if(!groupsByParentId.containsKey(group.getParentId())) {
          groupsByParentId.put(group.getParentId(), new ArrayList<>());  
        }
        groupsByParentId.get(group.getParentId()).add(group);
      }
    }
    
    // find bottom nodes
    for(final var group : allGroups) {
      final var children = groupsByParentId.get(group.getId());
      if(children == null || children.isEmpty()) {
        groupBottoms.add(group); 
      }
    }
    
    // group -> user roles by user id
    final var userRoles = new LinkedHashMap<String, List<OrgMemberRight>>();
    for(final var userRole : worldState.getMemberRights().values()) {
      if(!userRoles.containsKey(userRole.getMemberId())) {
        userRoles.put(userRole.getMemberId(), new ArrayList<>());  
      }
      userRoles.get(userRole.getMemberId()).add(userRole);
    }
    
    //group -> group roles by user id
    final var groupRoles = new LinkedHashMap<String, List<OrgPartyRight>>();
    for(final var groupRole : worldState.getPartyRights().values()) {
      if(!groupRoles.containsKey(groupRole.getPartyId())) {
        groupRoles.put(groupRole.getPartyId(), new ArrayList<>());  
      }
      groupRoles.get(groupRole.getPartyId()).add(groupRole);
    }

    // group -> group memberships by group id
    final var groupMemberships = new LinkedHashMap<String, List<OrgMembership>>();
    for(final var groupMembership : worldState.getMemberships().values()) {
      if(!groupMemberships.containsKey(groupMembership.getPartyId())) {
        groupMemberships.put(groupMembership.getPartyId(), new ArrayList<>());  
      }
      groupMemberships.get(groupMembership.getPartyId()).add(groupMembership);
    }
    
    final var userStatus = new LinkedHashMap<String, OrgActorStatus>();
    final var groupStatus = new LinkedHashMap<String, OrgActorStatus>();
    final var roleStatus = new LinkedHashMap<String, OrgActorStatus>();
    final var groupRoleStatus = new LinkedHashMap<String, OrgActorStatus>();
    final var userRoleStatus = new LinkedHashMap<String, OrgActorStatus>();
    final var membershipStatus = new LinkedHashMap<String, OrgActorStatus>();
    
    // load up all status combinations
    for(final var status : worldState.getActorStatus().values()) {
      // user status
      if(status.getMemberId() != null && status.getPartyId() == null && status.getRightId() == null) {
        userStatus.put(status.getMemberId(), status);
        continue;
      }
      
      // group status
      if(status.getMemberId() == null && status.getPartyId() != null && status.getRightId() == null) {
        groupStatus.put(status.getMemberId(), status);
        continue;
      }

      // role status
      if(status.getMemberId() == null && status.getPartyId() == null && status.getRightId() != null) {
        roleStatus.put(status.getMemberId(), status);
        continue;
      }
      
      // group membership status
      if(status.getMemberId() != null && status.getPartyId() != null && status.getRightId() == null) {
        membershipStatus.put(membershipStatusId(status.getPartyId(), status.getMemberId()), status);
        continue;
      }
      
      // group role
      if(status.getMemberId() == null && status.getPartyId() != null && status.getRightId() != null) {
        membershipStatus.put(groupRoleStatusId(status.getPartyId(), status.getRightId()), status);
        continue;
      }
      
      // user role
      if(status.getMemberId() != null && status.getPartyId() == null && status.getRightId() != null) {
        membershipStatus.put(userRoleStatusId(status.getMemberId(), status.getRightId()), status);
        continue;
      }
    }
    this.groupTops = Collections.unmodifiableList(groupTops);
    this.groupBottoms = Collections.unmodifiableList(groupBottoms);
    this.groupsByParentId = unmodifiableMap(groupsByParentId);
    this.userRoles = unmodifiableMap(userRoles);
    this.groupRoles = unmodifiableMap(groupRoles);
    this.groupMemberships = unmodifiableMap(groupMemberships);
    
    this.userStatus = Collections.unmodifiableMap(userStatus);
    this.groupStatus = Collections.unmodifiableMap(groupStatus);
    this.roleStatus = Collections.unmodifiableMap(roleStatus);
    this.groupRoleStatus = Collections.unmodifiableMap(groupRoleStatus);
    this.userRoleStatus = Collections.unmodifiableMap(userRoleStatus);
    this.membershipStatus = Collections.unmodifiableMap(membershipStatus);
    
    // inheritance
    final var inheritedMembersForParties = new LinkedHashMap<String, List<OrgMembership>>();
    for(final var rootNode : groupTops) {
      putAllInheritedMembers(rootNode, Collections.emptyList(), inheritedMembersForParties);
    }
    this.groupInheritedUsers = unmodifiableMap(inheritedMembersForParties);
  }
  

  private void putAllInheritedMembers(OrgParty party, List<OrgMembership> inheritedSoFar, Map<String, List<OrgMembership>> collector) {
    if(isStatusDisabled(getStatus(party))) {
      return;
    }
    collector.put(party.getId(), new ArrayList<>(inheritedSoFar));
    
    final List<OrgMembership> myChildrenWillInherit = ImmutableList.<OrgMembership>builder()
        .addAll(inheritedSoFar)
        .addAll(getPartyMemberships(party.getId()))
        .build();
    for(final var child : getPartyChildren(party.getId())) {
      putAllInheritedMembers(child, myChildrenWillInherit, collector); 
    }
  }

  private static final <T> Map<String, List<T>> unmodifiableMap(Map<String, List<T>> input) {
    final var result = new LinkedHashMap<String, List<T>>();
    for (var entry : input.entrySet()) {
      result.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
    }
    return Collections.unmodifiableMap(result);
  }
  
  @Override
  public List<OrgParty> getPartyTops() {
    return this.groupTops;
  }
  @Override
  public List<OrgParty> getPartyBottoms() {
    return this.groupBottoms;
  }
  @Override
  public OrgMember getMember(String id) {
    return worldState.getMembers().get(id);
  }
  @Override
  public List<OrgMemberRight> getMemberRights(String userId) {
    return Optional.ofNullable(userRoles.get(userId)).orElse(Collections.emptyList());
  }
  @Override
  public OrgParty getParty(String groupId) {
    return worldState.getParties().get(groupId);
  }
  @Override
  public List<OrgParty> getPartyChildren(String groupId) {
    return Optional.ofNullable(groupsByParentId.get(groupId)).orElse(Collections.emptyList());
  }
  @Override
  public List<OrgMembership> getPartyMemberships(String groupId) {
    return Optional.ofNullable(groupMemberships.get(groupId)).orElse(Collections.emptyList());
  }
  @Override
  public List<OrgPartyRight> getPartyRights(String groupId) {
    return Optional.ofNullable(groupRoles.get(groupId)).orElse(Collections.emptyList());
  }

  
  @Override
  public Optional<OrgActorStatus> getStatus(OrgParty group) {
    return Optional.ofNullable(groupStatus.get(group.getId()));
  }
  @Override
  public Optional<OrgActorStatus> getStatus(OrgMembership membership) {
    return Optional.ofNullable(membershipStatus.get(membershipStatusId(membership.getPartyId(), membership.getMemberId())));
  }
  @Override
  public Optional<OrgActorStatus> getStatus(OrgMemberRight role) {
    return Optional.ofNullable(userRoleStatus.get(userRoleStatusId(role.getMemberId(), role.getRightId())));
  }
  @Override
  public Optional<OrgActorStatus> getStatus(OrgPartyRight role) {
    return Optional.ofNullable(groupRoleStatus.get(groupRoleStatusId(role.getPartyId(), role.getRightId())));
  }
  @Override
  public Optional<OrgActorStatus> getStatus(OrgMember user) {
    return Optional.ofNullable(userStatus.get(user.getId()));
  }
  @Override
  public Optional<OrgActorStatus> getStatus(OrgRight role) {
    return Optional.ofNullable(roleStatus.get(role.getId()));
  }
  @Override  
  public boolean isStatusDisabled(Optional<OrgActorStatus> status) {
    if(status == null || status.isEmpty()) {
     return false;
    }
    return status.get().getValue() != OrgActorStatusType.IN_FORCE;
  }
  @Override  
  public boolean isPartyDisabledUpward(OrgParty group) {
    if(isStatusDisabled(getStatus(group))) {
      return true;
    }
    var parentId = group.getParentId();
    while(parentId != null) {
      final var next = worldState.getParties().get(parentId);      
      if(isStatusDisabled(getStatus(next))) {
        return true;
      }
      parentId = next.getParentId();
    }
    return false;
  }
  @Override
  public List<OrgMembership> getPartyInheritedMembers(String groupId) {
    return Optional.ofNullable(groupInheritedUsers.get(groupId)).orElse(Collections.emptyList());
  }
  private String membershipStatusId(String groupId, String userId) {
    return "group=" + groupId + ";user=" + userId;
  }
  private String groupRoleStatusId(String groupId, String roleId) {
    return "group=" + groupId + ";role=" + roleId;
  }
  private String userRoleStatusId(String userId, String roleId) {
    return "user=" + userId + ";role=" + roleId;
  }

  @Override
  public OrgRight getRight(String id) {
    return worldState.getRights().get(id);
  }

  @Override
  public Collection<OrgRight> getRights() {
    return worldState.getRights().values();
  }
}
