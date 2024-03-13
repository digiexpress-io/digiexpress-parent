package io.resys.thena.docdb.models.org.queries;

import io.resys.thena.docdb.api.DocDB.OrgModel.OrgProjectQuery;
import io.resys.thena.docdb.api.models.ImmutableOrgProjectObjects;
import io.resys.thena.docdb.api.models.ImmutableQueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgProjectObjects;
import io.resys.thena.docdb.models.org.OrgQueries;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrgProjectQueryImpl implements OrgProjectQuery {
  private final DbState state;
  private String repoId;

  @Override
  public OrgProjectQuery projectName(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "projectName can't be empty!");
    this.repoId = repoId;
    return this;
  }

  @Override
  public Uni<QueryEnvelope<OrgProjectObjects>> get() {
    RepoAssert.notEmpty(repoId, () -> "projectName can't be empty!");
    
    return state.project().getByNameOrId(repoId)
    .onItem().transformToUni((Repo existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(QueryEnvelope.repoNotFound(repoId, log));
      }
      return getProjectObjects(state.toOrgState().query(existing), existing)
        .onFailure().recoverWithItem(e -> QueryEnvelope.fatalError(existing, "Failed to fetch the world state", log, e));
    });
  }

  public Uni<QueryEnvelope<OrgProjectObjects>> getProjectObjects(OrgQueries org, Repo repo) {
    return Uni.combine().all().unis(
        org.groups().findAll().collect().asList(),
        org.users().findAll().collect().asList(),
        org.roles().findAll().collect().asList(),
        org.userMemberships().findAll().collect().asList(),
        org.groupRoles().findAll().collect().asList(),
        org.userRoles().findAll().collect().asList(),
        org.actorStatus().findAll().collect().asList()
    ).asTuple().onItem().transform(tuple -> {
      
      final var container = ImmutableOrgProjectObjects.builder();

      // GROUP 1
      tuple.getItem1().forEach(group -> container.putGroups(group.getId(), group));
      
      // USER 2
      tuple.getItem2().forEach(user -> container.putUsers(user.getId(), user));
      
      // ROLE 3
      tuple.getItem3().forEach(role -> container.putRoles(role.getId(), role));
      
      // MEMBERSHIP 4
      tuple.getItem4().forEach(member -> container.putUserMemberships(member.getId(), member));
      
      // GROUP_ROLE 5
      tuple.getItem5().forEach(groupRole -> container.putGroupRoles(groupRole.getId(), groupRole));
      
      // USER_ROLE 6
      tuple.getItem6().forEach(userRole -> container.putUserRoles(userRole.getId(), userRole));
      
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
