package io.dialob.client.spi.composer;

import io.dialob.client.api.DialobComposer.ComposerDocumentState;
import io.dialob.client.api.DialobComposer.ComposerState;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.dialob.client.api.DialobDocument.FormRevisionEntryDocument;
import io.dialob.client.api.DialobErrorHandler.DocumentNotFoundException;
import io.dialob.client.api.ImmutableComposerDocumentState;
import io.dialob.client.spi.exceptions.ErrorMsgBuilder;
import io.dialob.spi.Constants;

public class GetComposerDocumentState {

  private final ComposerState state;
  private final String idOrName;
  private final String version;
  
  public GetComposerDocumentState(ComposerState state, String idOrName, String version) {
    super();
    this.state = state;
    this.idOrName = idOrName;
    this.version = Constants.LATEST_REV.equals(version) ? null : version;
  }
  
  public ComposerDocumentState get() {
    for(final var rev : state.getRevs().values()) {
      final var foundFromRev = findFromRev(rev);
      if(foundFromRev != null) {
        return buildFromRev(rev);
      }
      
      final var foundFromDoc = findFromDocument(rev);
      if(foundFromDoc != null) {
        final var form = state.getForms().get(foundFromDoc.getFormId());
        return ImmutableComposerDocumentState.builder().revision(rev).form(form).build();
      }
    }
    
    throw notFound();
  }
  

  private ComposerDocumentState buildFromRev(FormRevisionDocument rev) {
    if(version == null) {
      final var form = state.getForms().get(rev.getHead());
      return ImmutableComposerDocumentState.builder().revision(rev).form(form).build();
    }
    
    for(final var entry : rev.getEntries()) {
      if(entry.getFormId().equals(this.version)) {
        final var form = state.getForms().get(entry.getFormId());
        return ImmutableComposerDocumentState.builder().revision(rev).form(form).build();
      }
      
      if(entry.getFormId().equals(this.version)) {
        final var form = state.getForms().get(entry.getFormId());
        return ImmutableComposerDocumentState.builder().revision(rev).form(form).build();
      }
      if(entry.getRevisionName().equals(this.version)) {
        final var form = state.getForms().get(entry.getFormId());
        return ImmutableComposerDocumentState.builder().revision(rev).form(form).build();
      }
    }
    throw notFound(); 
  }
  
  private FormRevisionEntryDocument findFromDocument(FormRevisionDocument rev) {
    for(final var entry : rev.getEntries()) {
      if(entry.getFormId().equals(this.idOrName)) {
        return entry;
      }
      
      if(entry.getFormId().equals(this.idOrName)) {
        return entry;
      }
    }
    
    return null;
  }

  private FormRevisionDocument findFromRev(FormRevisionDocument rev) {    
    if(rev.getId().equals(idOrName)) {
      return rev;
    }
    
    if(rev.getName().equals(idOrName)) {
      return rev;
    }
    
    return null;
  }
  
  private DocumentNotFoundException notFound() {
    return new DocumentNotFoundException(
        new ErrorMsgBuilder("Document not found!")
        .field("id/formName", idOrName)
        .field("version/tag", version)
        .build());
  }
}
