package io.resys.thena.structures.org.memberhierarchy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.org.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMemberHierarchyEntry;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgRightFlattened;


public class MemberTree {
  @JsonIgnore
  private final MemberTree parent;
  private final Map<String, MemberTree> children = new HashMap<>();
  
  // seed data
  private final String groupId;
  private final List<OrgMemberHierarchyEntry> groupValues = new ArrayList<>();
  private final Map<String, OrgRightFlattened> roleValues = new HashMap<>();
  
 
  private Boolean removed;
  private Boolean direct;

  public MemberTree(String groupId) {
    super();
    this.groupId = groupId;
    this.parent = null;
  }
  private MemberTree(MemberTree parent, String groupId) {
    super();
    this.groupId = groupId;
    this.parent = parent;
  }
  public Map<String, MemberTree> getChildren() { return Collections.unmodifiableMap(children); }
  public Map<String, OrgRightFlattened> getRoleValues() { return Collections.unmodifiableMap(roleValues); }
  public List<OrgMemberHierarchyEntry> getGroupValues() { return Collections.unmodifiableList(groupValues); }
  
  public String getGroupId() { return this.groupId; }
  public String getGroupName() { return this.groupValues.get(0).getPartyName(); }
  @JsonIgnore public MemberTree getParent() { return this.parent; }

  public MemberTree addChild(String groupId) {
    final var child = new MemberTree(this, groupId);
    children.put(groupId, child);
    return child;
  }
  public MemberTree getNode(String groupId) {
    if(this.groupId.equals(groupId)) {
      return this;
    }
    for(final var child : this.children.values()) {
      final var result = child.getNode(groupId);
      if(result != null) {
        return result;
      }
    }
    return null;
  }
  public void addGroupValue(OrgMemberHierarchyEntry next, OrgRightFlattened role) {
    groupValues.add(next); 
    if(role != null) {
      roleValues.put(role.getRightId(), role);
    }
  }
  @JsonIgnore
  public List<MemberTree> getLastNodes() {
    if(children.isEmpty()) {
      return Arrays.asList(this);
    }
    return children.values().stream()
        .flatMap(child -> child.getLastNodes().stream())
        .toList();
  }
  
  
  
  public boolean isGreyGroup() {
    if(this.removed != null) {
      return this.removed;
    }
    this.removed = calcGreyGroup();
    return this.removed;
  }
  public boolean isDirect() {
    if(this.direct != null) {
      return this.direct;
    }
    this.direct = this.groupValues.stream().filter(e -> e.getMembershipId() != null).count() > 0;
    return this.direct;
  }

  @JsonIgnore
  private boolean calcGreyGroup() {
    final var directRemoval = this.groupValues.stream()
        .filter(e -> e.getPartyStatus() != null)
        .filter(e -> e.getPartyStatus() != OrgActorStatusType.IN_FORCE)
        .count() > 0;
    
    if(directRemoval) {
      return true;
    }
    
    if(parent != null) {
      return parent.isGreyGroup();
    }
    return false;
  }
  
  @JsonIgnore
  public void close() {
    getLastNodes().forEach(node -> {
      node.isDirect();
      node.isGreyGroup();
    });
  }
}
