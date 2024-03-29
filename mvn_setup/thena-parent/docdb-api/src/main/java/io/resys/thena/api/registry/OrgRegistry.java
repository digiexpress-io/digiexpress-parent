package io.resys.thena.api.registry;

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

public interface OrgRegistry {
  OrgActorDataRegistry orgActorData();
  OrgActorStatusRegistry orgActorStatus();
  OrgCommitRegistry orgCommits();
  OrgCommitTreeRegistry orgCommitTrees();
  OrgMemberRightRegistry orgMemberRights();
  OrgMembershipRegistry orgMemberships();
  OrgMemberRegistry orgMembers();
  OrgPartyRightRegistry orgPartyRights();
  OrgPartyRegistry orgParties();
  OrgRightRegistry orgRights();
}
