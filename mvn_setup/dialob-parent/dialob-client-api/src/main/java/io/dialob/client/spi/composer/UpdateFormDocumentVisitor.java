package io.dialob.client.spi.composer;

import io.dialob.client.api.DialobComposer.ComposerDocumentState;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.ImmutableComposerDocumentState;
import io.dialob.client.spi.store.DialobDocumentStore;
import io.resys.thena.api.actions.DocCommitActions;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.spi.DocStoreException;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateFormDocumentVisitor  {
  private final FormDocument asset;
  private final DialobDocumentStore store;
  

  public Uni<DocCommitActions.OneDocEnvelope> start() {
    return store.getConfig().getClient()
        .doc(store.getConfig().getRepoId())
        .commit()
        .modifyOneBranch()
        .branchName(asset.getId())
        .commitMessage("Modify form")
        .commitAuthor(store.getConfig().getAuthor().get())
        .replace(JsonObject.mapFrom(asset.getData()))
        .build(); 
  }
  
  public ComposerDocumentState visitEnvelope(DocCommitActions.OneDocEnvelope envelope) {
    if(envelope.getStatus() != CommitResultStatus.OK) {
      throw DocStoreException.builder("FORM_UPDATE_FAIL")
        .add(envelope.getMessages())
        .add((callback) -> callback.addArgs(asset.getId()))
        .build();
    }
    final var doc = envelope.getDoc();
    final var branch = envelope.getBranch();
    
    final var form = GeComposerStateVisitor.mapToForm(doc, branch);
    final var revision = GeComposerStateVisitor.mergeRev(GeComposerStateVisitor.mapToRev(doc), form, branch);
    return ImmutableComposerDocumentState.builder().revision(revision).form(form).build();
  }
  
}
