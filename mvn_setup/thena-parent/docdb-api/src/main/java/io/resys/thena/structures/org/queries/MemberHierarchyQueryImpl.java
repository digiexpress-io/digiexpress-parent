package io.resys.thena.structures.org.queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.thena.api.actions.OrgQueryActions.MemberHierarchyQuery;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberHierarchyEntry;
import io.resys.thena.api.entities.org.OrgRightFlattened;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgMemberHierarchy;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.ImmutableQueryEnvelopeList;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.org.OrgState;
import io.resys.thena.structures.org.memberhierarchy.MemberTreeBuilder;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class MemberHierarchyQueryImpl implements MemberHierarchyQuery {
  private final DbState state;
  private final String repoId;

	@Override
	public Uni<QueryEnvelope<OrgMemberHierarchy>> get(String userId) {
		RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
		
    return state.toOrgState(repoId).onItem().transformToUni(orgState -> getUser(orgState, userId));
	}
  @Override
  public Uni<QueryEnvelopeList<OrgMemberHierarchy>> findAll() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    
    return this.state.toOrgState(repoId)
    .onItem().transformToUni(state -> {
      
      final Multi<OrgMember> users = state.query().members().findAll();
      final Multi<QueryEnvelope<OrgMemberHierarchy>> userAndGroups = users.onItem().transformToUni(user -> getUser(state, user.getId())).merge();
      return userAndGroups.collect().asList().onItem().transform(this::createUsersResult);
    });
  }
	
	private Uni<QueryEnvelope<OrgMemberHierarchy>> getUser(OrgState org, String userId) {
    return Uni.combine().all().unis(
        org.query().members().getById(userId),
        org.query().members().findAllRightsByMemberId(userId),
        org.query().members().findAllMemberHierarchyEntries(userId)
      ).asTuple()
      .onItem().transform(tuple -> createUserResult(tuple.getItem1(), tuple.getItem2(), tuple.getItem3()));
	}
	

	private QueryEnvelope<OrgMemberHierarchy> createUserResult(OrgMember user, List<OrgRightFlattened> roles, List<OrgMemberHierarchyEntry> groups) {
    return ImmutableQueryEnvelope
        .<OrgMemberHierarchy>builder()
        .status(QueryEnvelopeStatus.OK)
        .objects(new MemberTreeBuilder().member(user).partyData(groups).rightData(roles).build())
        .build();
	}
	
	private QueryEnvelopeList<OrgMemberHierarchy> createUsersResult(List<QueryEnvelope<OrgMemberHierarchy>> users) {
	  final var builder = ImmutableQueryEnvelopeList.<OrgMemberHierarchy>builder()
	      .status(QueryEnvelopeStatus.OK);
	  
	  final List<OrgMemberHierarchy> objects = new ArrayList<>();
	  for(final var resp : users) {
	    objects.add(resp.getObjects());
	    builder.addAllMessages(resp.getMessages());
	    
	    if(resp.getStatus() != QueryEnvelopeStatus.OK) {
	      builder.status(resp.getStatus());
	    }
	  }
	  
    return builder.objects(Collections.unmodifiableList(objects)).build();
  }
}
