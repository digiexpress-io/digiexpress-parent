package io.resys.thena.structures.org.queries;

import io.resys.thena.api.ThenaClient.OrgStructuredTenant.OrgProjectQuery;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.org.ImmutableOrgProjectObjects;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgProjectObjects;
import io.resys.thena.api.models.ImmutableQueryEnvelope;
import io.resys.thena.api.models.QueryEnvelope;
import io.resys.thena.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.org.OrgQueries;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrgProjectQueryImpl implements OrgProjectQuery {
  private final DbState state;
  private final String repoId;

  @Override
  public Uni<QueryEnvelope<OrgProjectObjects>> get() {
    RepoAssert.notEmpty(repoId, () -> "projectName can't be empty!");
    
    return state.project().getByNameOrId(repoId)
    .onItem().transformToUni((Tenant existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(QueryEnvelope.repoNotFound(repoId, log));
      }
      return getProjectObjects(state.toOrgState().query(existing), existing)
        .onFailure().recoverWithItem(e -> QueryEnvelope.fatalError(existing, "Failed to fetch the world state", log, e));
    });
  }

  private Uni<QueryEnvelope<OrgProjectObjects>> getProjectObjects(OrgQueries org, Tenant repo) {
    return Uni.combine().all().unis(
        org.parties().findAll().collect().asList(),
        org.members().findAll().collect().asList(),
        org.rights().findAll().collect().asList(),
        org.memberships().findAll().collect().asList(),
        org.partyRights().findAll().collect().asList(),
        org.memberRights().findAll().collect().asList(),
        org.actorStatus().findAll().collect().asList()
    ).asTuple().onItem().transform(tuple -> {
      
      final var container = ImmutableOrgProjectObjects.builder();

      // GROUP 1
      tuple.getItem1().forEach(group -> container.putParties(group.getId(), group));
      
      // USER 2
      tuple.getItem2().forEach(user -> container.putMembers(user.getId(), user));
      
      // ROLE 3
      tuple.getItem3().forEach(role -> container.putRights(role.getId(), role));
      
      // MEMBERSHIP 4
      tuple.getItem4().forEach(member -> container.putMemberships(member.getId(), member));
      
      // GROUP_ROLE 5
      tuple.getItem5().forEach(groupRole -> container.putPartyRights(groupRole.getId(), groupRole));
      
      // USER_ROLE 6
      tuple.getItem6().forEach(userRole -> container.putMemberRights(userRole.getId(), userRole));
      
      // ACTOR_STATUS 7
      tuple.getItem7().forEach(status -> container.putActorStatus(status.getId(), status));

      final QueryEnvelope<OrgProjectObjects> envelope = ImmutableQueryEnvelope.<OrgProjectObjects>builder()
          .objects(container.build())
          .status(QueryEnvelopeStatus.OK)
          .build();
      return envelope;
    });
  }
}
