package io.dialob.client.spi.composer;

import java.util.Collections;
import java.util.List;

import io.dialob.client.api.DialobComposer.ComposerState;
import io.dialob.client.api.DialobDocument;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormReleaseDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.dialob.client.api.DialobErrorHandler.DialobClientException;
import io.dialob.client.api.DialobErrorHandler.DocumentNotFoundException;
import io.dialob.client.api.DialobStore.StoreCommand;

public class DeleteEntityVisitor {

  private final String assetId;
  private final ComposerState state;
  
  public DeleteEntityVisitor(ComposerState state, String assetId, String assetVersion) {
    super();
    this.assetId = assetId;
    this.state = state;
  }
  public List<StoreCommand> visit() {
    final var asset = visitId();
    final var bodyType = asset.getType();
    
    switch (bodyType) {
    case FORM: return visitForm((FormDocument) asset);
    case FORM_REV: return visitFormRev((FormRevisionDocument) asset);
    case RELEASE: return visitRelease((FormReleaseDocument) asset);
    default: throw new DeleteDocumentException("Unknown asset of type: '" + bodyType + "'!"); 
    }
  }
  
  private DialobDocument visitId() {
    if(state.getForms().containsKey(assetId)) {
      return state.getForms().get(assetId);
      
    } else if(state.getRevs().containsKey(assetId)) {
      return state.getRevs().get(assetId);
      
    } else if(state.getReleases().containsKey(assetId)) {
      return state.getReleases().get(assetId);
    }
    
    throw new DocumentNotFoundException("No entity with id: '" + assetId + "'");
  }
  
  private List<StoreCommand> visitFormRev(FormRevisionDocument form) {
    // TODO
    
    return Collections.emptyList();
  }

  private List<StoreCommand> visitForm(FormDocument flowTask) {
    // TODO
    
    return Collections.emptyList();
  }

  private List<StoreCommand> visitRelease(FormReleaseDocument decision) {
    // TODO
    
    return Collections.emptyList();
  }
  
  public static class DeleteDocumentException extends RuntimeException implements DialobClientException {
    private static final long serialVersionUID = 7193990799948041231L;
    public DeleteDocumentException(String message) {
      super(message);
    }
  }
}
