package io.resys.sysconfig.client.spi.visitors;

import java.util.Arrays;
import java.util.List;

import io.resys.sysconfig.client.api.model.Document;
import io.resys.sysconfig.client.api.model.ImmutableSysConfigRelease;
import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.resys.sysconfig.client.spi.store.DocumentConfig;
import io.resys.sysconfig.client.spi.store.DocumentConfig.DocObjectVisitor;
import io.resys.sysconfig.client.spi.store.DocumentStoreException;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.models.QueryEnvelope;
import io.resys.thena.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.models.ThenaDocObject.Doc;
import io.resys.thena.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.api.models.ThenaDocObject.DocLog;
import io.resys.thena.api.models.ThenaDocObjects.DocObject;
import io.resys.thena.projects.client.spi.store.MainBranch;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GetSysConfigReleaseByIdVisitor implements DocObjectVisitor<SysConfigRelease> {
  private final String projectId;
  
  @Override
  public DocObjectsQuery start(DocumentConfig config, DocObjectsQuery builder) {
    return builder
        .docType(Document.DocumentType.SYS_CONFIG_RELEASE.name())
        .branchName(MainBranch.HEAD_NAME)
        .matchIds(Arrays.asList(projectId));
  }

  @Override
  public DocObject visitEnvelope(DocumentConfig config, QueryEnvelope<DocObject> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("GET_SYS_CONFIG_RELEASE_BY_ID_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(projectId))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw DocumentStoreException.builder("GET_SYS_CONFIG_RELEASE_BY_ID_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(projectId))
        .build();
    }
    return result;
  }

  @Override
  public SysConfigRelease end(DocumentConfig config, DocObject ref) {
    return ref.accept((Doc doc, DocBranch docBranch, DocCommit commit, List<DocLog> log) -> 
      docBranch.getValue()
      .mapTo(ImmutableSysConfigRelease.class).withVersion(commit.getId())
    ).get(0);
  }
}
