package io.resys.thena.registry.org;

import io.resys.thena.api.registry.OrgRegistry;
import io.resys.thena.api.registry.org.OrgActorDataRegistry;
import io.resys.thena.api.registry.org.OrgActorStatusRegistry;
import io.resys.thena.api.registry.org.OrgCommitRegistry;
import io.resys.thena.api.registry.org.OrgCommitTreeRegistry;
import io.resys.thena.api.registry.org.OrgMemberRegistry;
import io.resys.thena.api.registry.org.OrgMemberRightRegistry;
import io.resys.thena.api.registry.org.OrgMembershipRegistry;
import io.resys.thena.api.registry.org.OrgPartyRegistry;
import io.resys.thena.api.registry.org.OrgPartyRightRegistry;
import io.resys.thena.api.registry.org.OrgRightRegistry;
import io.resys.thena.datasource.TenantTableNames;

public class OrgRegistrySqlImpl implements OrgRegistry {
  @SuppressWarnings("unused")
  private final TenantTableNames options;
  private final OrgActorDataRegistry orgActorData;
  private final OrgActorStatusRegistry orgActorStatus;
  private final OrgCommitRegistry orgCommits;
  private final OrgCommitTreeRegistry orgCommitTrees;
  private final OrgMemberRightRegistry orgMemberRights;
  private final OrgMembershipRegistry orgMemberships;
  private final OrgMemberRegistry orgMembers;
  private final OrgPartyRightRegistry orgPartyRights;
  private final OrgPartyRegistry orgParties;
  private final OrgRightRegistry orgRights;
  
  public OrgRegistrySqlImpl(TenantTableNames options) {
    this.options = options;  
    orgActorData = new OrgActorDataRegistrySqlImpl(options);
    orgActorStatus = new OrgActorStatusRegistrySqlImpl(options);
    orgCommits = new OrgCommitRegistrySqlImpl(options);
    orgCommitTrees =new OrgCommitTreeRegistrySqlImpl(options);
    orgMemberRights = new OrgMemberRightRegistrySqlImpl(options);
    orgMemberships = new OrgMembershipRegistrySqlImpl(options);
    orgMembers = new OrgMemberRegistrySqlImpl(options);
    orgPartyRights =new OrgPartyRightRegistrySqlImpl(options);
    orgParties = new OrgPartyRegistrySqlImpl(options);
    orgRights = new OrgRightRegistrySqlImpl(options);
  }
  
  @Override
  public OrgActorDataRegistry orgActorData() {
    return orgActorData;
  }

  @Override
  public OrgActorStatusRegistry orgActorStatus() {
    return orgActorStatus;
  }

  @Override
  public OrgCommitRegistry orgCommits() {
    return orgCommits;
  }

  @Override
  public OrgCommitTreeRegistry orgCommitTrees() {
    return orgCommitTrees;
  }

  @Override
  public OrgMemberRightRegistry orgMemberRights() {
    return orgMemberRights;
  }

  @Override
  public OrgMembershipRegistry orgMemberships() {
    return orgMemberships;
  }

  @Override
  public OrgMemberRegistry orgMembers() {
    return orgMembers;
  }

  @Override
  public OrgPartyRightRegistry orgPartyRights() {
    return orgPartyRights;
  }

  @Override
  public OrgPartyRegistry orgParties() {
    return orgParties;
  }

  @Override
  public OrgRightRegistry orgRights() {
    return orgRights;
  }

}
