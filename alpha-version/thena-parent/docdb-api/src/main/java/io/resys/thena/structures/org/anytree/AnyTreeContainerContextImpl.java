package io.resys.thena.structures.org.anytree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import io.resys.thena.api.entities.org.OrgActorStatusType;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgProjectObjects;
import io.resys.thena.api.envelope.OrgTreeContainer.OrgAnyTreeContainerContext;

public class AnyTreeContainerContextImpl implements OrgAnyTreeContainerContext {
  
  private final OrgProjectObjects worldState;
  private final List<OrgParty> partyTops;
  private final List<OrgParty> partyBottoms;
  private final Map<String, List<OrgParty>> partysByParentId;
  private final Map<String, List<OrgMemberRight>> userRights;
  private final Map<String, List<OrgMembership>> partyMemberships;
  private final Map<String, List<OrgPartyRight>> partyRights;
  private final Map<String, List<OrgMembership>> partyInheritedUsers;
  
  
  public AnyTreeContainerContextImpl(OrgProjectObjects worldState) {
    super();
    this.worldState = worldState;
    
    final var partyTops = new ArrayList<OrgParty>();
    final var partyBottoms = new ArrayList<OrgParty>();
    final var partysByParentId = new LinkedHashMap<String, List<OrgParty>>();
    final var allGroups = worldState.getParties().values();
    
    for(final var party : allGroups) {
      if(party.getParentId() == null) {
        partyTops.add(party);    
      } 
      
      if(party.getParentId() != null) {
        if(!partysByParentId.containsKey(party.getParentId())) {
          partysByParentId.put(party.getParentId(), new ArrayList<>());  
        }
        partysByParentId.get(party.getParentId()).add(party);
      }
    }
    
    // find bottom nodes
    for(final var party : allGroups) {
      final var children = partysByParentId.get(party.getId());
      if(children == null || children.isEmpty()) {
        partyBottoms.add(party); 
      }
    }
    
    // party -> user roles by user id
    final var userRights = new LinkedHashMap<String, List<OrgMemberRight>>();
    for(final var userRole : worldState.getMemberRights().values()) {
      if(!userRights.containsKey(userRole.getMemberId())) {
        userRights.put(userRole.getMemberId(), new ArrayList<>());  
      }
      userRights.get(userRole.getMemberId()).add(userRole);
    }
    
    //party -> party roles by user id
    final var partyRights = new LinkedHashMap<String, List<OrgPartyRight>>();
    for(final var partyRole : worldState.getPartyRights().values()) {
      if(!partyRights.containsKey(partyRole.getPartyId())) {
        partyRights.put(partyRole.getPartyId(), new ArrayList<>());  
      }
      partyRights.get(partyRole.getPartyId()).add(partyRole);
    }

    // party -> party memberships by party id
    final var partyMemberships = new LinkedHashMap<String, List<OrgMembership>>();
    for(final var partyMembership : worldState.getMemberships().values()) {
      if(!partyMemberships.containsKey(partyMembership.getPartyId())) {
        partyMemberships.put(partyMembership.getPartyId(), new ArrayList<>());  
      }
      partyMemberships.get(partyMembership.getPartyId()).add(partyMembership);
    }
    
    
    this.partyTops = Collections.unmodifiableList(partyTops);
    this.partyBottoms = Collections.unmodifiableList(partyBottoms);
    this.partysByParentId = unmodifiableMap(partysByParentId);
    this.userRights = unmodifiableMap(userRights);
    this.partyRights = unmodifiableMap(partyRights);
    this.partyMemberships = unmodifiableMap(partyMemberships);
    
    // inheritance
    final var inheritedMembersForParties = new LinkedHashMap<String, List<OrgMembership>>();
    for(final var rootNode : partyTops) {
      putAllInheritedMembers(rootNode, Collections.emptyList(), inheritedMembersForParties);
    }
    this.partyInheritedUsers = unmodifiableMap(inheritedMembersForParties);
  }
  

  @Override
  public List<OrgMemberRight> getMembersWithRights(String rightId) {
    return worldState.getMemberRights()
        .values().stream()
        .filter(right -> right.getRightId().equals(rightId))
        .filter(right -> right.getPartyId() == null)
        .toList();
  }

  private void putAllInheritedMembers(OrgParty party, List<OrgMembership> inheritedSoFar, Map<String, List<OrgMembership>> collector) {
    if(party.getStatus() == OrgActorStatusType.DISABLED) {
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
    return this.partyTops;
  }
  @Override
  public List<OrgParty> getPartyBottoms() {
    return this.partyBottoms;
  }
  @Override
  public OrgMember getMember(String id) {
    return worldState.getMembers().get(id);
  }
  @Override
  public List<OrgMemberRight> getMemberRights(String userId) {
    return Optional.ofNullable(userRights.get(userId)).orElse(Collections.emptyList());
  }
  @Override
  public OrgParty getParty(String partyId) {
    return worldState.getParties().get(partyId);
  }
  @Override
  public List<OrgParty> getPartyChildren(String partyId) {
    return Optional.ofNullable(partysByParentId.get(partyId)).orElse(Collections.emptyList());
  }
  @Override
  public List<OrgMembership> getPartyMemberships(String partyId) {
    return Optional.ofNullable(partyMemberships.get(partyId)).orElse(Collections.emptyList());
  }
  @Override
  public List<OrgPartyRight> getPartyRights(String partyId) {
    return Optional.ofNullable(partyRights.get(partyId)).orElse(Collections.emptyList());
  }

  @Override  
  public boolean isPartyDisabledUpward(OrgParty party) {
    if(party.getStatus() == OrgActorStatusType.DISABLED) {
      return true;
    }
    var parentId = party.getParentId();
    while(parentId != null) {
      final var next = worldState.getParties().get(parentId);      
      if(next.getStatus() == OrgActorStatusType.DISABLED) {
        return true;
      }
      parentId = next.getParentId();
    }
    return false;
  }
  @Override
  public List<OrgMembership> getPartyInheritedMembers(String partyId) {
    return Optional.ofNullable(partyInheritedUsers.get(partyId)).orElse(Collections.emptyList());
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
