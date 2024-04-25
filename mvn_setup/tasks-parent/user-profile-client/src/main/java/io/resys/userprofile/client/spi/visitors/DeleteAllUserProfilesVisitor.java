package io.resys.userprofile.client.spi.visitors;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocBranches;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocs;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.spi.ThenaDocConfig.DocObjectsVisitor;
import io.resys.userprofile.client.api.model.ImmutableUserProfile;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.spi.support.DataConstants;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteAllUserProfilesVisitor implements DocObjectsVisitor<Uni<List<UserProfile>>>{

  
  private ModifyManyDocBranches archiveCommand;
  private ModifyManyDocs removeCommand;
  
  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> start(ThenaDocConfig config, DocObjectsQuery query) {
    this.removeCommand = config.getClient().doc(config.getRepoId()).commit().modifyManyDocs()
        .commitAuthor(config.getAuthor().get())
        .commitMessage("Delete Tenants");
    
    // Build the blob criteria for finding all documents of type Project
    return query.docType(DataConstants.DOC_TYPE_USER_PROFILE).findAll();
  }

  @Override
  public DocTenantObjects visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocStoreException.builder("FIND_ALL_USER_PROFILES_FAIL_FOR_DELETE").add(config, envelope).build();
    }
    return envelope.getObjects();
  }
  
  @Override
  public Uni<List<UserProfile>> end(ThenaDocConfig config, DocTenantObjects ref) {
    if(ref == null) {
      return Uni.createFrom().item(Collections.emptyList());
    }

    final var profilesRemoved = visitTree(ref);    
    return archiveCommand.build()
      .onItem().transform((ManyDocsEnvelope commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new DocStoreException("USER_PROFILE_ARCHIVE_FAIL", DocStoreException.convertMessages(commit));
      })
      .onItem().transformToUni(archived -> removeCommand.build())
      .onItem().transform((ManyDocsEnvelope commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new DocStoreException("USER_PROFILE_REMOVE_FAIL", DocStoreException.convertMessages(commit));
      })
      .onItem().transform((commit) -> profilesRemoved);
  }

  
  
  
  private List<UserProfile> visitTree(DocTenantObjects state) {
    return state.getBranches().values().stream()
      .map(blob -> blob.getValue().mapTo(ImmutableUserProfile.class))
      .map(UserProfile -> visitUserProfile(UserProfile))
      .collect(Collectors.toUnmodifiableList());
  }
  private UserProfile visitUserProfile(UserProfile userProfile) {
    
    // final var json = JsonObject.mapFrom(userProfile);
    removeCommand.item().docId(userProfile.getId()).remove();
    return userProfile;
  }


}
