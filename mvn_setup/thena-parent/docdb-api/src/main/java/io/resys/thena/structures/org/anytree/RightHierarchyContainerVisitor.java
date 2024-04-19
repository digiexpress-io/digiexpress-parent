package io.resys.thena.structures.org.anytree;

import java.util.List;

import org.apache.commons.lang3.mutable.MutableObject;

import io.resys.thena.api.entities.org.ImmutableOrgRightHierarchy;
import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.api.entities.org.OrgActorStatus.OrgActorStatusType;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgRightHierarchy;
import io.resys.thena.api.envelope.OrgPartyContainerVisitor;
import io.resys.thena.api.envelope.OrgRightsLogVisitor;
import io.resys.thena.api.envelope.OrgTreeContainer.OrgAnyTreeContainerContext;
import io.resys.thena.api.envelope.OrgTreeContainer.OrgAnyTreeContainerVisitor;
import lombok.RequiredArgsConstructor;



public class RightHierarchyContainerVisitor extends OrgPartyContainerVisitor<OrgRightHierarchy> 
  implements OrgAnyTreeContainerVisitor<OrgRightHierarchy> {
  
  private final String roleIdOrNameOrExternalId;
  private final ImmutableOrgRightHierarchy.Builder builder = ImmutableOrgRightHierarchy.builder();
  private final StringBuilder log = new StringBuilder();
  private final MutableObject<Boolean> foundParty = new MutableObject<>(false);
  
  private OrgAnyTreeContainerContext ctx;
  private OrgRight target;
  
  public RightHierarchyContainerVisitor(String roleIdOrNameOrExternalId, boolean includeDisabled) {
    super(includeDisabled, true);
    this.roleIdOrNameOrExternalId = roleIdOrNameOrExternalId;
  }
  public void start(OrgAnyTreeContainerContext ctx) {
    
    final var target = ctx.getRights().stream().filter(role -> role.isMatch(roleIdOrNameOrExternalId)).findAny();
    if(target.isEmpty()) {
      final var msg = new StringBuilder("Can't build role hierarchy!").append(System.lineSeparator()).append("Can't find the role!").append(System.lineSeparator())
          .append("  - tried to match by, id or name or external id: ").append(roleIdOrNameOrExternalId).append(System.lineSeparator())
          .append("  - known roles: ").append(System.lineSeparator());
      
      for(final var role : ctx.getRights()) {
        msg
          .append("    - existing role ").append(System.lineSeparator())
          .append("      id: ").append(role.getId()).append(System.lineSeparator())
          .append("      name: ").append(role.getRightName()).append(System.lineSeparator())
          .append("      external id: ").append(role.getExternalId()).append(System.lineSeparator());
      }
      
      throw new RoleNotFoundForHierarchyException(msg.toString());
    }
    this.target = target.get();
    this.ctx = ctx;
    super.start(ctx);
    
  }
  
  @Override
  public OrgRightHierarchy close() {
    final var result = builder
        .status(ctx.getStatus(target).map(status -> status.getValue()).orElse(OrgActorStatus.OrgActorStatusType.IN_FORCE))
        .log(log.toString())
        .roleId(target.getId())
        .commitId(target.getCommitId())
        .externalId(target.getExternalId())
        .roleName(target.getRightName())
        .roleDescription(target.getRightDescription())
        .addAllDirectMembers(ctx.getMembersWithRights(target.getId()).stream()
            .filter(member -> includeDisabled || ctx.getStatus(member).map(s -> s.getValue() == OrgActorStatusType.IN_FORCE).orElse(true))
            .map(right -> ctx.getMember(right.getMemberId()))
            .filter(member -> includeDisabled || ctx.getStatus(member).map(s -> s.getValue() == OrgActorStatusType.IN_FORCE).orElse(true))
            .toList()
        ).build();
    
    
    return result
        .withChildMembers(result.getChildMembers().stream().distinct().toList())
        .withChildParty(result.getChildParty().stream().distinct().toList())
        .withDirectMembers(result.getDirectMembers().stream().distinct().toList())
        .withDirectParty(result.getDirectParty().stream().distinct().toList())
        ;
  }

  @Override
  protected TopPartyVisitor visitTop(OrgParty group, OrgAnyTreeContainerContext worldState) {
    return new CollectDirectPartiesAndMembersForRight(target, ctx, builder, this.log, this.foundParty);
  }

  @Override
  protected PartyVisitor visitChild(OrgParty group, OrgAnyTreeContainerContext worldState) {
    if(this.foundParty.getValue()) {
      return new CollectAll(target, worldState, builder);
    } else {
      return new CollectDirectPartiesAndMembersForRight(target, ctx, builder, this.log, this.foundParty);
    }
  }
  
  @RequiredArgsConstructor
  private static class CollectAll implements TopPartyVisitor {
    private final OrgRight target;
    private final OrgAnyTreeContainerContext ctx;
    private final ImmutableOrgRightHierarchy.Builder builder;

    @Override 
    public void start(OrgParty party, List<OrgParty> parents, List<OrgRight> parentRights, boolean isDisabled) {
      builder.addChildParty(party);
    }
    
    @Override
    public void visitDirectMembership(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled) {
      builder.addChildMembers(user);
    }
    
    @Override
    public void visitDirectMemberPartyRight(OrgParty group, OrgMemberRight memberRight, OrgRight right, boolean isDisabled) {
      final var member = ctx.getMember(memberRight.getMemberId());
      if(right.getId().equals(target.getId())) {
        builder.addDirectMembers(member);
      }
    }
    @Override public void visitDirectPartyRight(List<OrgParty> parents, OrgParty group, OrgPartyRight partyRight, OrgRight right, boolean isDisabled) {}
    @Override public void end(OrgParty group, List<OrgParty> parents, boolean isDisabled) {}
    @Override public void visitMembershipWithInheritance(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled) {}
    @Override public void visitChildParty(OrgParty group, boolean isDisabled) {}
    @Override public TopPartyLogger visitLogger(OrgParty party) { return null; }
  }
  
  
  @RequiredArgsConstructor
  private static class CollectDirectPartiesAndMembersForRight implements TopPartyVisitor {
    private final OrgRight target;
    private final OrgAnyTreeContainerContext ctx;
    private final ImmutableOrgRightHierarchy.Builder builder;
    private final StringBuilder log;
    private final MutableObject<Boolean> foundParty;

    @Override
    public void visitDirectMembership(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled) {
      if(foundParty.getValue()) {
        builder.addDirectMembers(user);
      }
    }
    @Override
    public void visitDirectPartyRight(List<OrgParty> parents, OrgParty group, OrgPartyRight partyRight, OrgRight right, boolean isDisabled) {
      if(right.getId().equals(target.getId())) {
        foundParty.setValue(true);
      }
    }
    
    @Override
    public void visitDirectMemberPartyRight(OrgParty group, OrgMemberRight memberRight, OrgRight right, boolean isDisabled) {
      if(right.getId().equals(target.getId())) {
        builder.addDirectMembers(ctx.getMember(memberRight.getMemberId()));
      }
    }
    @Override
    public void end(OrgParty group, List<OrgParty> parents, boolean isDisabled) {
      if(foundParty.getValue()) {
        builder.addDirectParty(group);
      }
      foundParty.setValue(false);
    }

    @Override public void start(OrgParty party, List<OrgParty> parents, List<OrgRight> parentRights, boolean isDisabled) {}
    @Override public void visitMembershipWithInheritance(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled) {}
    @Override public void visitChildParty(OrgParty group, boolean isDisabled) {}
    @Override public TopPartyLogger visitLogger(OrgParty party) {
      return new OrgRightsLogVisitor(target) {
        @Override
        public String close() {
          final var logData = super.close();
          log.append(logData);
          return logData;
        }
      };
    
    }
  }
  
  public static class RoleNotFoundForHierarchyException extends RuntimeException {
    private static final long serialVersionUID = 88546975164711743L;
    public RoleNotFoundForHierarchyException(String message) {
      super(message);
    }   
  }
}
