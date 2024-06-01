package io.dialob.client.spi.composer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.dialob.client.api.DialobComposer.ComposerState;
import io.dialob.client.api.DialobDocument;
import io.dialob.client.api.ImmutableComposerState;
import io.dialob.client.api.ImmutableFormDocument;
import io.dialob.client.api.ImmutableFormRevisionDocument;
import io.dialob.client.api.ImmutableFormRevisionEntryDocument;
import io.dialob.client.spi.store.DialobDocumentStore.DialobDocumentStoreTypes;
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
import io.resys.thena.structures.doc.actions.DocObjectsQueryImpl;
import io.smallrye.mutiny.Uni;

public class GeComposerStateVisitor implements DocObjectsVisitor<ComposerState> {
  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> start(ThenaDocConfig config, DocObjectsQuery builder) {
    return builder.docType(DialobDocumentStoreTypes.FORM.name()).findAll();
  }
  @Override
  public DocTenantObjects visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK || envelope.getObjects() == null) {
      throw DocStoreException.builder("FIND_COMPOSER_STATE_FAIL").add(config, envelope).build();
    }
    return envelope.getObjects();
  }

  @Override
  public ComposerState end(ThenaDocConfig config, DocTenantObjects ref) {
    final var builder = ImmutableComposerState.builder();
    final var revisions = new HashMap<String, DialobDocument.FormRevisionDocument>(); 
    
    ref.accept((Doc doc, DocBranch docBranch, Map<String, DocCommit> commit, List<DocCommands> commands, List<DocCommitTree> trees) -> {
      
      // form data - each branch is its own form
      final var form = mapToForm(doc, docBranch);
      builder.putForms(form.getId(), form);
      
      // revision data all branches of one document
      if(!revisions.containsKey(doc.getId())) {
        revisions.put(doc.getId(), mapToRev(doc));
      }

      final var mergedRev = mergeRev(revisions.get(doc.getId()), form, docBranch);
      revisions.put(doc.getId(), mergedRev);
      
      return null;
    });
    return builder.putAllRevs(revisions).build();
  }

  public static DialobDocument.FormRevisionDocument mergeRev(DialobDocument.FormRevisionDocument start, DialobDocument.FormDocument form, DocBranch docBranch) {
    final var next = ImmutableFormRevisionDocument.builder().from(start);

    // main dev branch
    if(docBranch.getBranchName().equals(DocObjectsQueryImpl.BRANCH_MAIN)) {
      next.head(form.getId());
    }
    
    next.addEntries(ImmutableFormRevisionEntryDocument.builder()
        .created(form.getCreated())
        .updated(form.getUpdated())
        .revisionName(docBranch.getBranchName())
        .formId(form.getId())
        .build());
    
    return next.build();
  }
  
  public static DialobDocument.FormRevisionDocument mapToRev(Doc doc) {
    return ImmutableFormRevisionDocument.builder()
    .id(doc.getId())
    .version(doc.getCommitId())
    .created(doc.getCreatedAt().toLocalDateTime())
    .updated(doc.getUpdatedAt().toLocalDateTime())
    .name(doc.getExternalId())
    .head("")
    .build();
  }
  public static DialobDocument.FormDocument mapToForm(Doc doc, DocBranch docBranch) {
    final var form = docBranch.getValue().mapTo(io.dialob.api.form.ImmutableForm.class);
    return ImmutableFormDocument.builder()
        .id(docBranch.getId())
        .version(docBranch.getCommitId())
        .created(doc.getCreatedAt().toLocalDateTime())
        .updated(docBranch.getUpdatedAt().toLocalDateTime())
        .name(doc.getExternalId())
        
        .data(form)
        .build();
  }
}
