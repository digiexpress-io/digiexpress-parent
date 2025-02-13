package io.resys.avatar.client.spi.visitors;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.resys.avatar.client.api.Avatar;
import io.resys.avatar.client.api.ImmutableAvatar;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.spi.ThenaDocConfig.DocObjectsVisitor;
import io.smallrye.mutiny.Uni;

public class FindAllAvatarsVisitor implements DocObjectsVisitor<List<Avatar>> {
  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> start(ThenaDocConfig config, DocObjectsQuery builder) {
    return builder.findAll();
  }
  @Override
  public DocTenantObjects visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocStoreException.builder("FIND_ALL_AVATARS_FAIL").add(config, envelope).build();
    }
    return envelope.getObjects();
  }

  @Override
  public List<Avatar> end(ThenaDocConfig config, DocTenantObjects ref) {
    if(ref == null) {
      return Collections.emptyList();
    }
    return ref.accept((
        Doc doc, 
        DocBranch docBranch, 
        Map<String, DocCommit> commit, 
        List<DocCommands> commands,
        List<DocCommitTree> trees) -> mapToAvatar(docBranch)
    );
  }
  
  public static ImmutableAvatar mapToAvatar(DocBranch docBranch) {
    return docBranch.getValue()
      .mapTo(ImmutableAvatar.class)
      .withVersion(docBranch.getCommitId())
      .withCreated(docBranch.getCreatedAt().toInstant())
      .withUpdated(docBranch.getUpdatedAt().toInstant());
  }

}
