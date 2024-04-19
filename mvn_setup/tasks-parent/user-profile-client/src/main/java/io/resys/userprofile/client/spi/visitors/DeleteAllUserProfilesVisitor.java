package io.resys.userprofile.client.spi.visitors;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocBranches;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocs;
import io.resys.thena.api.actions.DocQueryActions;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.userprofile.client.api.model.Document;
import io.resys.userprofile.client.api.model.ImmutableUserProfile;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.spi.store.UserProfileStoreConfig;
import io.resys.userprofile.client.spi.store.UserProfileStoreConfig.DocObjectsVisitor;
import io.resys.userprofile.client.spi.store.DocumentStoreException;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteAllUserProfilesVisitor implements DocObjectsVisitor<Uni<List<UserProfile>>>{

  
  private ModifyManyDocBranches archiveCommand;
  private ModifyManyDocs removeCommand;
  
  @Override
  public DocObjectsQuery start(UserProfileStoreConfig config, DocObjectsQuery query) {
    this.removeCommand = config.getClient().doc(config.getRepoId()).commit().modifyManyDocs()
        .author(config.getAuthor().get())
        .message("Delete Tenants");
    
    // Build the blob criteria for finding all documents of type Project
    return query.docType(Document.DocumentType.USER_PROFILE.name());
  }

  @Override
  public DocQueryActions.DocObjects visitEnvelope(UserProfileStoreConfig config, QueryEnvelope<DocQueryActions.DocObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("FIND_ALL_USER_PROFILES_FAIL_FOR_DELETE").add(config, envelope).build();
    }
    return envelope.getObjects();
  }
  
  @Override
  public Uni<List<UserProfile>> end(UserProfileStoreConfig config, DocQueryActions.DocObjects ref) {
    if(ref == null) {
      return Uni.createFrom().item(Collections.emptyList());
    }

    final var profilesRemoved = visitTree(ref);    
    return archiveCommand.build()
      .onItem().transform((ManyDocsEnvelope commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new DocumentStoreException("USER_PROFILE_ARCHIVE_FAIL", DocumentStoreException.convertMessages(commit));
      })
      .onItem().transformToUni(archived -> removeCommand.build())
      .onItem().transform((ManyDocsEnvelope commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new DocumentStoreException("USER_PROFILE_REMOVE_FAIL", DocumentStoreException.convertMessages(commit));
      })
      .onItem().transform((commit) -> profilesRemoved);
  }

  
  
  
  private List<UserProfile> visitTree(DocQueryActions.DocObjects state) {
    return state.getBranches().values().stream().flatMap(e -> e.stream())
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
