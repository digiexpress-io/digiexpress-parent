package io.resys.thena.docdb.models.org.anytree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgProjectObjects;
import io.resys.thena.docdb.api.visitors.OrgTreeContainer.OrgAnyTreeContainerContext;

public class AnyTreeContainerContextImpl implements OrgAnyTreeContainerContext {
  
  private final OrgProjectObjects worldState;
  private final List<OrgGroup> groupTops;
  private final List<OrgGroup> groupBottoms;
  private final Map<String, List<OrgGroup>> groupsByParentId;
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
    
    final var groupTops = new ArrayList<OrgGroup>();
    final var groupBottoms = new ArrayList<OrgGroup>();
    final var groupsByParentId = new LinkedHashMap<String, List<OrgGroup>>();
    final var allGroups = worldState.getGroups().values();
    
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
    for(final var userRole : worldState.getUserRoles().values()) {
      if(!userRoles.containsKey(userRole.getUserId())) {
        userRoles.put(userRole.getUserId(), new ArrayList<>());  
      }
      userRoles.get(userRole.getUserId()).add(userRole);
    }
    
    //group -> group roles by user id
    final var groupRoles = new LinkedHashMap<String, List<OrgPartyRight>>();
    for(final var groupRole : worldState.getGroupRoles().values()) {
      if(!groupRoles.containsKey(groupRole.getGroupId())) {
        groupRoles.put(groupRole.getGroupId(), new ArrayList<>());  
      }
      groupRoles.get(groupRole.getGroupId()).add(groupRole);
    }

    // group -> group memberships by group id
    final var groupMemberships = new LinkedHashMap<String, List<OrgMembership>>();
    for(final var groupMembership : worldState.getUserMemberships().values()) {
      if(!groupMemberships.containsKey(groupMembership.getGroupId())) {
        groupMemberships.put(groupMembership.getGroupId(), new ArrayList<>());  
      }
      groupMemberships.get(groupMembership.getGroupId()).add(groupMembership);
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
      if(status.getUserId() != null && status.getGroupId() == null && status.getRoleId() == null) {
        userStatus.put(status.getUserId(), status);
        continue;
      }
      
      // group status
      if(status.getUserId() == null && status.getGroupId() != null && status.getRoleId() == null) {
        groupStatus.put(status.getUserId(), status);
        continue;
      }

      // role status
      if(status.getUserId() == null && status.getGroupId() == null && status.getRoleId() != null) {
        roleStatus.put(status.getUserId(), status);
        continue;
      }
      
      // group membership status
      if(status.getUserId() != null && status.getGroupId() != null && status.getRoleId() == null) {
        membershipStatus.put(membershipStatusId(status.getGroupId(), status.getUserId()), status);
        continue;
      }
      
      // group role
      if(status.getUserId() == null && status.getGroupId() != null && status.getRoleId() != null) {
        membershipStatus.put(groupRoleStatusId(status.getGroupId(), status.getRoleId()), status);
        continue;
      }
      
      // user role
      if(status.getUserId() != null && status.getGroupId() == null && status.getRoleId() != null) {
        membershipStatus.put(userRoleStatusId(status.getUserId(), status.getRoleId()), status);
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
    final var groupInheritedUsers = new LinkedHashMap<String, List<OrgMembership>>();
    
    for(final var bottom : groupBottoms) {
      if(isGroupDisabledUpward(bottom)) {
        continue;
      }
      var parentId = bottom.getParentId();
      final var users = new ArrayList<OrgMembership>();
      while(parentId != null) {
        final var next = worldState.getGroups().get(parentId);
        if(isGroupDisabledUpward(bottom)) {
          continue;
        }
        groupInheritedUsers.put(next.getId(), Collections.unmodifiableList(users.stream().distinct().toList()));
        users.addAll(getGroupMemberships(next.getId()).stream().filter(m -> !isStatusDisabled(getStatus(m))).toList());
        parentId = next.getParentId();
      }
    }
    
    
    
    this.groupInheritedUsers = unmodifiableMap(groupInheritedUsers);
  }

  private final <T> Map<String, List<T>> unmodifiableMap(Map<String, List<T>> input) {
    final var result = new LinkedHashMap<String, List<T>>();
    for (var entry : input.entrySet()) {
      result.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
    }
    return Collections.unmodifiableMap(result);
  }
  
  @Override
  public List<OrgGroup> getGroupTops() {
    return this.groupTops;
  }
  @Override
  public List<OrgGroup> getGroupBottoms() {
    return this.groupBottoms;
  }
  @Override
  public OrgMember getUser(String id) {
    return worldState.getUsers().get(id);
  }
  @Override
  public List<OrgMemberRight> getUserRoles(String userId) {
    return Optional.ofNullable(userRoles.get(userId)).orElse(Collections.emptyList());
  }
  @Override
  public OrgGroup getGroup(String groupId) {
    return worldState.getGroups().get(groupId);
  }
  @Override
  public List<OrgGroup> getGroupChildren(String groupId) {
    return Optional.ofNullable(groupsByParentId.get(groupId)).orElse(Collections.emptyList());
  }
  @Override
  public List<OrgMembership> getGroupMemberships(String groupId) {
    return Optional.ofNullable(groupMemberships.get(groupId)).orElse(Collections.emptyList());
  }
  @Override
  public List<OrgPartyRight> getGroupRoles(String groupId) {
    return Optional.ofNullable(groupRoles.get(groupId)).orElse(Collections.emptyList());
  }

  
  @Override
  public Optional<OrgActorStatus> getStatus(OrgGroup group) {
    return Optional.ofNullable(groupStatus.get(group.getId()));
  }
  @Override
  public Optional<OrgActorStatus> getStatus(OrgMembership membership) {
    return Optional.ofNullable(membershipStatus.get(membershipStatusId(membership.getGroupId(), membership.getUserId())));
  }
  @Override
  public Optional<OrgActorStatus> getStatus(OrgMemberRight role) {
    return Optional.ofNullable(userRoleStatus.get(userRoleStatusId(role.getUserId(), role.getRoleId())));
  }
  @Override
  public Optional<OrgActorStatus> getStatus(OrgPartyRight role) {
    return Optional.ofNullable(groupRoleStatus.get(groupRoleStatusId(role.getGroupId(), role.getRoleId())));
  }
  @Override
  public Optional<OrgActorStatus> getStatus(OrgMember user) {
    return Optional.ofNullable(userStatus.get(user.getId()));
  }
  @Override
  public Optional<OrgActorStatus> getStatus(OrgRole role) {
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
  public boolean isGroupDisabledUpward(OrgGroup group) {
    if(isStatusDisabled(getStatus(group))) {
      return true;
    }
    var parentId = group.getParentId();
    while(parentId != null) {
      final var next = worldState.getGroups().get(parentId);      
      if(isStatusDisabled(getStatus(next))) {
        return true;
      }
      parentId = next.getParentId();
    }
    return false;
  }
  @Override
  public List<OrgMembership> getGroupInheritedUsers(String groupId) {
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
  public OrgRole getRole(String id) {
    return worldState.getRoles().get(id);
  }

  @Override
  public Collection<OrgRole> getRoles() {
    return worldState.getRoles().values();
  }
}
