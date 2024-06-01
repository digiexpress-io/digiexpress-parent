package io.dialob.client.spi.composer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobComposer.ComposerDocumentState;
import io.dialob.client.api.DialobComposer.ComposerState;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobErrorHandler.DatabaseException;
import io.dialob.client.api.ImmutableComposerDocumentState;
import io.dialob.client.spi.exceptions.ErrorMsgBuilder;
import io.dialob.client.spi.store.DialobDocumentStore.DialobDocumentStoreTypes;
import io.dialob.client.spi.support.OidUtils;
import io.resys.thena.api.actions.DocCommitActions.CreateManyDocs;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.spi.ThenaDocConfig.DocCreateVisitor;
import io.resys.thena.structures.doc.actions.DocObjectsQueryImpl;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateFormDocumentVisitor implements DocCreateVisitor<ComposerDocumentState> {

  private final ComposerState state;
  private final FormDocument asset;
  @SuppressWarnings("unused")
  private final DialobClient client;
  
  private ManyDocsEnvelope envelope;
  
  @Override
  public CreateManyDocs start(ThenaDocConfig config, CreateManyDocs builder) {
    visitValidations();
    
    final var formId = asset.getId() == null ? OidUtils.gen() : asset.getId();
    
    return builder
    .commitAuthor(config.getAuthor().get())
    .commitMessage("creating new form")
    .item()
      .docId(formId)
      .docType(DialobDocumentStoreTypes.FORM.name())
      .branchContent(JsonObject.mapFrom(asset.getData()))
      .branchName(DocObjectsQueryImpl.BRANCH_MAIN)
      .next();
  }

  @Override
  public List<DocBranch> visitEnvelope(ThenaDocConfig config, ManyDocsEnvelope envelope) {
    this.envelope = envelope;
    if(envelope.getStatus() == CommitResultStatus.OK) {
      return envelope.getBranch();
    }
    
    throw new DocStoreException("FORM_CREATE_FAIL", DocStoreException.convertMessages(envelope));
  }

  @Override
  public List<ComposerDocumentState> end(ThenaDocConfig config, List<DocBranch> branches) {
    final var doc = this.envelope.getDoc().iterator().next();
    final var branch = this.envelope.getBranch().iterator().next();
    
    final var form = GeComposerStateVisitor.mapToForm(doc, branch);
    final var revision = GeComposerStateVisitor.mergeRev(GeComposerStateVisitor.mapToRev(doc), form, branch);
    
    
    final var state = ImmutableComposerDocumentState.builder().revision(revision).form(form).build();
    return Collections.unmodifiableList(Arrays.asList(state));
  }


  private void visitValidations() {
    if(StringUtils.isEmpty(asset.getData().getName().trim())) {
      throw new DatabaseException(
          new ErrorMsgBuilder("Document name not valid valid!")
          .field("provided name", "'" + asset.getData() + "'")
          .build());
    }
    
    if(asset.getData().getId() != null && this.state.getForms().containsKey(asset.getData().getId())) {
      throw new DatabaseException(
          new ErrorMsgBuilder("Document id not valid valid!")
          .field("provided id", "'" + asset.getData() + "' already exists!")
          .build());
    }
    
    
    final var decision = state.getRevs().values().stream()
      .filter(e -> e.getName().trim().equals(asset.getData().getName().trim()))
      .findFirst();
    if(decision.isPresent()) {
      throw new DatabaseException(
          new ErrorMsgBuilder("Document name not valid valid!")
          .field("existing id", "'" + decision.get().getId() + "'")
          .field("existing name", "'" + decision.get().getName() + "'")
          .field("provided name", "'" + asset.getData() + "'")
          .build());
    }
  }
}
