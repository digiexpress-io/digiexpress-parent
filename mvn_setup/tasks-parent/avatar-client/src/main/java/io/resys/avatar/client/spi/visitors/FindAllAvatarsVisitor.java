package io.resys.avatar.client.spi.visitors;

import java.util.Collections;
import java.util.List;

import io.resys.avatar.client.api.Avatar;
import io.resys.avatar.client.spi.store.AvatarStoreConfig;
import io.resys.avatar.client.spi.store.AvatarStoreException;
import io.resys.avatar.client.spi.store.AvatarStoreConfig.AvatarDocObjectsVisitor;
import io.resys.thena.api.actions.DocQueryActions;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.userprofile.client.api.ImmutableAvatar;

public class FindAllAvatarsVisitor implements AvatarDocObjectsVisitor<List<Avatar>> {
  @Override
  public DocObjectsQuery start(AvatarStoreConfig config, DocObjectsQuery builder) {
    return builder;
  }
  @Override
  public DocQueryActions.DocObjects visitEnvelope(AvatarStoreConfig config, QueryEnvelope<DocQueryActions.DocObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw AvatarStoreException.builder("FIND_ALL_AVATARS_FAIL").add(config, envelope).build();
    }
    return envelope.getObjects();
  }

  @Override
  public List<Avatar> end(AvatarStoreConfig config, DocQueryActions.DocObjects ref) {
    if(ref == null) {
      return Collections.emptyList();
    }
    return ref.accept((Doc doc, DocBranch docBranch, DocCommit commit, List<DocLog> log) -> {
      
      return docBranch.getValue().mapTo(ImmutableAvatar.class);
    });
  }
}
