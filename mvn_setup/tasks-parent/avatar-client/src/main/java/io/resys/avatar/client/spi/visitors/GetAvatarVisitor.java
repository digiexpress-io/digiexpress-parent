package io.resys.avatar.client.spi.visitors;

import java.util.List;

import io.resys.avatar.client.api.Avatar;
import io.resys.avatar.client.spi.store.AvatarStoreConfig;
import io.resys.avatar.client.spi.store.AvatarStoreException;
import io.resys.avatar.client.spi.store.AvatarStoreConfig.AvatarDocObjectVisitor;
import io.resys.thena.api.actions.DocQueryActions;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.userprofile.client.api.ImmutableAvatar;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GetAvatarVisitor implements AvatarDocObjectVisitor<Avatar>{
  private final String id;
  
  @Override
  public DocObjectsQuery start(AvatarStoreConfig config, DocObjectsQuery query) {
    return query.matchId(id);
  }

  @Override
  public DocQueryActions.DocObject visitEnvelope(AvatarStoreConfig config, QueryEnvelope<DocQueryActions.DocObject> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw AvatarStoreException.builder("GET_AVATAR_BY_ID_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(id))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw AvatarStoreException.builder("GET_AVATAR_BY_ID_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(id))
        .build();
    }
    return result;
  }

  @Override
  public Avatar end(AvatarStoreConfig config, DocQueryActions.DocObject ref) {
    return ref.accept((Doc doc, DocBranch docBranch, DocCommit commit, List<DocLog> log) -> 
        docBranch.getValue()
          .mapTo(ImmutableAvatar.class)
          .withVersion(docBranch.getCommitId())
          .withExternalId(doc.getExternalId())
        ).iterator().next();
  }
}
