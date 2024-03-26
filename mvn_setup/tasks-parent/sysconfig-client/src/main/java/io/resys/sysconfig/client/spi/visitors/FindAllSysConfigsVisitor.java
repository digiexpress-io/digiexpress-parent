package io.resys.sysconfig.client.spi.visitors;

import java.util.Collections;
import java.util.List;

import io.resys.sysconfig.client.api.model.Document;
import io.resys.sysconfig.client.api.model.ImmutableSysConfig;
import io.resys.sysconfig.client.api.model.SysConfig;
import io.resys.sysconfig.client.spi.store.DocumentConfig;
import io.resys.sysconfig.client.spi.store.DocumentConfig.DocObjectsVisitor;
import io.resys.sysconfig.client.spi.store.DocumentStoreException;
import io.resys.thena.api.actions.DocQueryActions;
import io.resys.thena.api.actions.DocQueryActions.DocObjects;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.projects.client.spi.store.MainBranch;

public class FindAllSysConfigsVisitor implements DocObjectsVisitor<List<SysConfig>> {
  @Override
  public DocObjectsQuery start(DocumentConfig config, DocObjectsQuery builder) {
    return builder
        .docType(Document.DocumentType.SYS_CONFIG.name())
        .branchName(MainBranch.HEAD_NAME);
  }
  @Override
  public DocQueryActions.DocObjects visitEnvelope(DocumentConfig config, QueryEnvelope<DocQueryActions.DocObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("FIND_ALL_SYS_CONFIGS_FAIL").add(config, envelope).build();
    }
    return envelope.getObjects();
  }

  @Override
  public List<SysConfig> end(DocumentConfig config, DocQueryActions.DocObjects ref) {
    if(ref == null) {
      return Collections.emptyList();
    }
    return ref.accept((Doc doc, DocBranch docBranch, DocCommit commit, List<DocLog> log) -> docBranch.getValue().mapTo(ImmutableSysConfig.class));
  }
}
