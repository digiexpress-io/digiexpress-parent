package io.resys.sysconfig.client.spi.visitors;

import java.util.Arrays;
import java.util.List;

import io.resys.sysconfig.client.api.model.Document;
import io.resys.sysconfig.client.api.model.ImmutableSysConfigRelease;
import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.resys.thena.api.actions.DocQueryActions;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.projects.client.spi.store.MainBranch;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.spi.ThenaDocConfig.DocObjectVisitor;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GetSysConfigReleaseByIdVisitor implements DocObjectVisitor<SysConfigRelease> {
  private final String projectId;
  
  @Override
  public DocObjectsQuery start(ThenaDocConfig config, DocObjectsQuery builder) {
    return builder
        .docType(Document.DocumentType.SYS_CONFIG_RELEASE.name())
        .branchName(MainBranch.HEAD_NAME)
        .matchIds(Arrays.asList(projectId));
  }

  @Override
  public DocQueryActions.DocObject visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocQueryActions.DocObject> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocStoreException.builder("GET_SYS_CONFIG_RELEASE_BY_ID_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(projectId))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw DocStoreException.builder("GET_SYS_CONFIG_RELEASE_BY_ID_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(projectId))
        .build();
    }
    return result;
  }

  @Override
  public SysConfigRelease end(ThenaDocConfig config, DocQueryActions.DocObject ref) {
    return ref.accept((Doc doc, DocBranch docBranch, DocCommit commit, List<DocLog> log) -> 
      docBranch.getValue()
      .mapTo(ImmutableSysConfigRelease.class).withVersion(commit.getId())
    ).get(0);
  }
}
