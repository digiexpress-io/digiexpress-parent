package io.resys.thena.docdb.models.org.queries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.barfuin.texttree.api.DefaultNode;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyles;

import io.resys.thena.docdb.api.actions.OrgQueryActions.UserGroupsAndRolesQuery;
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ImmutableOrgUserGroupsAndRolesWithLog;
import io.resys.thena.docdb.api.models.ImmutableQueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaEnvelope;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupAndRoleFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserGroupsAndRolesWithLog;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrgUserGroupsAndRolesQueryImpl implements UserGroupsAndRolesQuery {
  private final DbState state;
  private String repoId;

  @Override
  public UserGroupsAndRolesQuery repoId(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    this.repoId = repoId;
    return this;
  }

	@Override
	public Uni<QueryEnvelope<OrgUserGroupsAndRolesWithLog>> get(String userId) {
		RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
		
    return state.project().getByNameOrId(repoId)
    .onItem().transformToUni((Repo existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(repoNotFound());
      }
      
      return state.toOrgState().query(repoId).onItem()
      		.transformToUni(state -> state.users().findAllGroupsAndRolesByUserId(userId))
      		.onItem()
      		.transform(entries -> createResult(entries));
    });
	}

	private QueryEnvelope<OrgUserGroupsAndRolesWithLog> createResult(List<OrgGroupAndRoleFlattened> entries) {
    return ImmutableQueryEnvelope
        .<OrgUserGroupsAndRolesWithLog>builder()
        .status(QueryEnvelopeStatus.OK)
        .objects(new StructureBuilder(entries).build())
        .build();
	}
	
	

  private <T extends ThenaEnvelope.ThenaObjects> QueryEnvelope<T> repoNotFound() {
    final var ex = RepoException.builder().notRepoWithName(repoId);
    log.warn(ex.getText());
    return ImmutableQueryEnvelope
        .<T>builder()
        .status(QueryEnvelopeStatus.ERROR)
        .addMessages(ex)
        .build();
  }
  
  
  private <T extends ThenaEnvelope.ThenaObjects> QueryEnvelope<T> docNotFound(Repo existing) {
    return ImmutableQueryEnvelope.<T>builder()
    .repo(existing)
    .status(QueryEnvelopeStatus.ERROR)
    .addMessages(ImmutableMessage.builder()
        .text(new StringBuilder()
            .append("User groups and roles not found by given id, from repo: '").append(existing.getId()).append("'!")
            .toString())
        .build())
    .build();
  }
  
  @Slf4j
  private static class StructureBuilder {

  	private final Map<String, OrgGroupAndRoleFlattened> byGroupId = new HashMap<>();
  	private final Map<String, List<OrgGroupAndRoleFlattened>> byParentGroupId = new HashMap<>();
  	private final List<OrgGroupAndRoleFlattened> roots = new ArrayList<>();
  	private final ImmutableOrgUserGroupsAndRolesWithLog.Builder result = ImmutableOrgUserGroupsAndRolesWithLog.builder();
  	
  	
  	public StructureBuilder(List<OrgGroupAndRoleFlattened> entries) {
  		
  		for(final var entry : entries) {
  			byGroupId.put(entry.getGroupId(), entry);
  			if(entry.getGroupParentId() == null) {
  				roots.add(entry);
  				continue;
  			}
				if(!byParentGroupId.containsKey(entry.getGroupParentId())) {
					byParentGroupId.put(entry.getGroupParentId(), new ArrayList<>());
				}
				byParentGroupId.get(entry.getGroupParentId()).add(entry);
  		}
  	}
  	
  	private void visitRoot(OrgGroupAndRoleFlattened entry) {
  		final var options = new TreeOptions();
  		options.setStyle(TreeStyles.UNICODE_ROUNDED);
  		final var tree = new DefaultNode(entry.getGroupName());
  		visitChildren(entry, tree);
  		final var rendered = TextTree.newInstance(options).render(tree);
  		log.error(System.lineSeparator() +
  				"##############################" + System.lineSeparator() +
  				rendered
  				);
  	}
  	
  	private void visitChild(OrgGroupAndRoleFlattened child, DefaultNode parentNode) {
  		final var childNode = new DefaultNode(child.getGroupName());
  		parentNode.addChild(childNode);
  		visitChildren(child, childNode);
  	}
  	
  	private void visitChildren(OrgGroupAndRoleFlattened parentEntry, DefaultNode parentNode) {
  		final var children = byParentGroupId.get(parentEntry.getGroupId());
  		if(children == null) {
  			return;
  		}
  		for(final var child : children) {
  			visitChild(child, parentNode);
  		}
  	}
  	
  	private OrgUserGroupsAndRolesWithLog build() {
  		for(final var root : this.roots) {
  			visitRoot(root);
  		}
  		return null;
  	}
  }
}
