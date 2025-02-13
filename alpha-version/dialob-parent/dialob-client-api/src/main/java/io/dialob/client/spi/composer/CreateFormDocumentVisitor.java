package io.dialob.client.spi.composer;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobComposer.ComposerState;
import io.dialob.client.api.DialobDocument.DocumentType;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobErrorHandler.DatabaseException;
import io.dialob.client.api.DialobStore.StoreCommand;
import io.dialob.client.api.ImmutableCreateStoreEntity;
import io.dialob.client.api.ImmutableFormRevisionDocument;
import io.dialob.client.spi.exceptions.ErrorMsgBuilder;
import io.dialob.client.spi.support.DialobAssert;
import io.dialob.client.spi.support.OidUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateFormDocumentVisitor {

  private final ComposerState state;
  private final FormDocument asset;
  private final DialobClient client;
  
  public List<StoreCommand> visit() {
    DialobAssert.notNull(asset, () -> "asset can't be null!");
    DialobAssert.notNull(state, () -> "state can't be null!");
    DialobAssert.notNull(client, () -> "client can't be null!");
    visitValidations();
    
    final var created = LocalDateTime.now();
    final var formId = asset.getId() == null ? OidUtils.gen() : asset.getId();
    
    
    final var rev = ImmutableFormRevisionDocument.builder()
      .id(null).version(null)
      .name(asset.getData().getName())    
      .head(formId)
      .created(created).updated(created)
      .build();
    
    return Arrays.asList(
        ImmutableCreateStoreEntity.builder()
          .body(client.getConfig().getMapper().toStoreBody(rev))
          .bodyType(DocumentType.FORM_REV)
          .build(),
        ImmutableCreateStoreEntity.builder()
          .id(formId).version(asset.getVersion())
          .body(client.getConfig().getMapper().toStoreBody(asset))
          .bodyType(DocumentType.FORM)
          .build());
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
