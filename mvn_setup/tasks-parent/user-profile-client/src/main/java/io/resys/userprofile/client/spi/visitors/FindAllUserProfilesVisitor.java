package io.resys.userprofile.client.spi.visitors;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.entities.doc.ThenaDocConfig;
import io.resys.thena.api.entities.doc.ThenaDocConfig.DocObjectsVisitor;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.userprofile.client.api.model.ImmutableUserProfile;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.spi.store.UserProfileStoreException;
import io.resys.userprofile.client.spi.support.DataConstants;
import io.smallrye.mutiny.Uni;

public class FindAllUserProfilesVisitor implements DocObjectsVisitor<List<UserProfile>> {
  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> start(ThenaDocConfig config, DocObjectsQuery builder) {
    return builder.docType(DataConstants.DOC_TYPE_USER_PROFILE).findAll();
  }
  @Override
  public DocTenantObjects visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw UserProfileStoreException.builder("FIND_ALL_USER_PROFILES_FAIL").add(config, envelope).build();
    }
    return envelope.getObjects();
  }

  @Override
  public List<UserProfile> end(ThenaDocConfig config, DocTenantObjects ref) {
    if(ref == null) {
      return Collections.emptyList();
    }
    return ref.accept((
        Doc doc, 
        DocBranch docBranch, 
        Map<String, DocCommit> commit, 
        List<DocCommands> commands,
        List<DocCommitTree> trees) -> mapToUserProfile(docBranch)
    );
  }
  
  public static ImmutableUserProfile mapToUserProfile(DocBranch docBranch) {
    return docBranch.getValue()
      .mapTo(ImmutableUserProfile.class)
      .withVersion(docBranch.getCommitId())
      .withCreated(docBranch.getCreatedAt().toInstant())
      .withUpdated(docBranch.getUpdatedAt().toInstant());
  }
}
