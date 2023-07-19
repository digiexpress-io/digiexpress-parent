package io.dialob.client.spi.composer;

import java.time.LocalDateTime;
import java.util.List;

import io.dialob.api.form.ImmutableForm;
import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobComposer.ComposerState;
import io.dialob.client.api.DialobErrorHandler.DocumentNotFoundException;
import io.dialob.client.api.DialobStore.StoreCommand;
import io.dialob.client.api.ImmutableFormDocument;
import io.dialob.client.spi.exceptions.ErrorMsgBuilder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CopyAsEntityVisitor {

  private final ComposerState state; 
  private final String assetId;
  private final DialobClient client;
  private final String copyToName;


  public List<StoreCommand> visit() {
    final var src = state.getForms().get(assetId);
    if(src == null) {
      throw notFound();
    }
    final var newForm = ImmutableForm.builder()
        .from(src.getData())
        .id(null)
        .rev(null)
        .name(copyToName)
        .build();
    final var newDoc = ImmutableFormDocument.builder()
        .name(copyToName)
        .created(LocalDateTime.now())
        .updated(LocalDateTime.now())
        .description(src.getDescription())
        .data(newForm)
        .build();
    return new CreateFormDocumentVisitor(state, newDoc, client).visit();
  }

  
  private DocumentNotFoundException notFound() {
    return new DocumentNotFoundException(
        new ErrorMsgBuilder("Document not found!")
        .field("form id", assetId)
        .field("copyToName", copyToName)
        .build());
  }
}
