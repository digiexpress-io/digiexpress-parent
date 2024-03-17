package io.resys.thena.docdb.models.org.queries;

import java.util.ArrayList;
import java.util.Collections;

import io.resys.thena.docdb.api.actions.OrgQueryActions.GroupHierarchyQuery;
import io.resys.thena.docdb.api.models.ImmutableQueryEnvelope;
import io.resys.thena.docdb.api.models.ImmutableQueryEnvelopeList;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.QueryEnvelopeList;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgGroupHierarchy;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgProjectObjects;
import io.resys.thena.docdb.models.org.anytree.AnyTreeContainerContextImpl;
import io.resys.thena.docdb.models.org.anytree.AnyTreeContainerImpl;
import io.resys.thena.docdb.models.org.anytree.GroupHierarchyContainerVisitor;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrgGroupHierarchyQueryImpl implements GroupHierarchyQuery {
  private final DbState state;
  private String repoId;
  
  @Override
  public GroupHierarchyQuery repoId(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    this.repoId = repoId;
    return this;
  }
  @Override
  public Uni<QueryEnvelope<OrgGroupHierarchy>> get(String groupIdOrNameOrExternalId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    
    return new OrgProjectQueryImpl(state).projectName(repoId).get()
        .onItem().transform(resp -> {
          if(resp.getStatus() != QueryEnvelopeStatus.OK) {
            return resp.toType();
          }
          
          try {
            final QueryEnvelope<OrgGroupHierarchy> success = createGroupHierarchy(resp, groupIdOrNameOrExternalId);
            return success;
          } catch(Exception e) {
            return QueryEnvelope.fatalError(resp.getRepo(), "Failed to build hierarchy for all groups", log, e);
          }
        });
  }
  @Override
  public Uni<QueryEnvelopeList<OrgGroupHierarchy>> findAll() {
    return new OrgProjectQueryImpl(state).projectName(repoId).get()
        .onItem().transform(resp -> {
          if(resp.getStatus() != QueryEnvelopeStatus.OK) {
            return resp.toListOfType();
          }
          
          try {
            final QueryEnvelopeList<OrgGroupHierarchy> success = createGroupHierarchy(resp);
            return success;
          } catch(Exception e) {
            final QueryEnvelope<OrgGroupHierarchy> fail = QueryEnvelope.fatalError(resp.getRepo(), "Failed to build hierarchy for all groups", log, e);
            return fail.toList();
          }
        });
  }

  private QueryEnvelopeList<OrgGroupHierarchy> createGroupHierarchy(QueryEnvelope<OrgProjectObjects> init) {
    final var groups = new ArrayList<OrgGroupHierarchy>();
    final var ctx = new AnyTreeContainerContextImpl(init.getObjects());
    final var container = new AnyTreeContainerImpl<OrgGroupHierarchy>(ctx);
    for(final var criteria : init.getObjects().getGroups().values().stream().sorted((a, b) -> a.getGroupName().compareTo(b.getGroupName())).toList()) {
      final OrgGroupHierarchy group = container.accept(new GroupHierarchyContainerVisitor(criteria.getId()));
      groups.add(group);
    }
    return ImmutableQueryEnvelopeList.<OrgGroupHierarchy>builder()
        .objects(Collections.unmodifiableList(groups))
        .repo(init.getRepo())
        .status(QueryEnvelope.QueryEnvelopeStatus.OK)
        .build();
  }
  
  private QueryEnvelope<OrgGroupHierarchy> createGroupHierarchy(
      QueryEnvelope<OrgProjectObjects> init, 
      String groupIdOrNameOrExternalId) {
    
    final var ctx = new AnyTreeContainerContextImpl(init.getObjects());
    final OrgGroupHierarchy group = new AnyTreeContainerImpl<OrgGroupHierarchy>(ctx).accept(new GroupHierarchyContainerVisitor(groupIdOrNameOrExternalId));
    return ImmutableQueryEnvelope.<OrgGroupHierarchy>builder()
        .objects(group)
        .repo(init.getRepo())
        .status(QueryEnvelope.QueryEnvelopeStatus.OK)
        .build();
  }
}