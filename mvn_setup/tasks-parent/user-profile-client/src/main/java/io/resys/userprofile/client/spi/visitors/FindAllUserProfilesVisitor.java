package io.resys.userprofile.client.spi.visitors;

import java.util.Collections;
import java.util.List;

import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.doc.ThenaDocObject.Doc;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocBranch;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocCommit;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocLog;
import io.resys.thena.api.entities.doc.ThenaDocObjects.DocObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.userprofile.client.api.model.Document;
import io.resys.userprofile.client.api.model.ImmutableUserProfile;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.spi.store.DocumentConfig;
import io.resys.userprofile.client.spi.store.DocumentConfig.DocObjectsVisitor;
import io.resys.userprofile.client.spi.store.DocumentStoreException;
import io.resys.userprofile.client.spi.store.MainBranch;

public class FindAllUserProfilesVisitor implements DocObjectsVisitor<List<UserProfile>> {
  @Override
  public DocObjectsQuery start(DocumentConfig config, DocObjectsQuery builder) {
    return builder
        .docType(Document.DocumentType.USER_PROFILE.name())
        .branchName(MainBranch.HEAD_NAME);
  }
  @Override
  public DocObjects visitEnvelope(DocumentConfig config, QueryEnvelope<DocObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("FIND_ALL_USER_PROFILES_FAIL").add(config, envelope).build();
    }
    return envelope.getObjects();
  }

  @Override
  public List<UserProfile> end(DocumentConfig config, DocObjects ref) {
    if(ref == null) {
      return Collections.emptyList();
    }
    return ref.accept((Doc doc, DocBranch docBranch, DocCommit commit, List<DocLog> log) -> docBranch.getValue().mapTo(ImmutableUserProfile.class));
  }
}
