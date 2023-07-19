package io.dialob.client.spi.composer;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobComposer.ComposerState;
import io.dialob.client.api.DialobDocument.DocumentType;
import io.dialob.client.api.DialobDocument.FormReleaseDocument;
import io.dialob.client.api.DialobDocument.FormReleaseValueDocument;
import io.dialob.client.api.DialobErrorHandler.DialobClientException;
import io.dialob.client.api.DialobStore.StoreCommand;
import io.dialob.client.api.ImmutableCreateStoreEntity;
import io.dialob.client.api.ImmutableEmptyCommand;
import io.dialob.client.api.ImmutableFormReleaseDocument;
import io.dialob.client.api.ImmutableUpdateStoreEntity;
import io.dialob.client.spi.exceptions.ErrorMsgBuilder;

public class ImportReleaseVisitor {

  private final DialobClient client;
  private final FormReleaseDocument asset;
  private final ComposerState state;
  private final List<String> skipOnForms = new ArrayList<>();

  public ImportReleaseVisitor(ComposerState state, FormReleaseDocument asset, DialobClient client) {
    super();
    this.asset = asset;
    this.state = state;
    this.client = client;
  }
  
  public List<StoreCommand> visit() {
    
    final var revisions = asset.getValues().stream()
      .filter(e -> e.getBodyType() == DocumentType.FORM_REV)
      .map(this::visitBody)
      .flatMap(e -> e.stream())
      .collect(Collectors.toList());
    
    final var forms = asset.getValues().stream()
        .filter(e -> e.getBodyType() == DocumentType.FORM)
        .map(this::visitBody)
        .flatMap(e -> e.stream())
        .collect(Collectors.toList());
      
    final var result = new ArrayList<StoreCommand>(initRelease(asset));
    result.addAll(revisions);
    result.addAll(forms);
    return Collections.unmodifiableList(result);
  }
  
  private List<StoreCommand> visitBody(FormReleaseValueDocument original) {
    switch (original.getBodyType()) {
    case FORM: return initForm(original);
    case FORM_REV: return initFormRev(original);
    case RELEASE: return Collections.emptyList();
    default: throw new ImportReleaseDocumentException("Unknown asset: '" + original.getBodyType() + "'!"); 
    }
  }

  public List<StoreCommand> initForm(FormReleaseValueDocument original) {
    
    final var newFormDocument = this.client.getConfig().getMapper().readFormDoc(original.getCommands());
    if(skipOnForms.contains(newFormDocument.getId())) {
      return Arrays.asList(ImmutableEmptyCommand.builder()
          .bodyType(original.getBodyType())
          .id(newFormDocument.getId())
          .description(new ErrorMsgBuilder("Form document can't be updated because revision conflict")
              .field("id", newFormDocument.getId())
              .build())
          .build());
    }
    
    final var oldDocument = state.getForms().get(newFormDocument.getId());
    
    if(oldDocument == null) {
      final var body = this.client.getConfig().getMapper().toStoreBody(newFormDocument);
      return Arrays.asList(ImmutableCreateStoreEntity.builder()
          .body(body)
          .bodyType(original.getBodyType())
          .id(newFormDocument.getId())
          .version(newFormDocument.getVersion())
          .build());
    }
    
    if(!oldDocument.getVersion().equals(newFormDocument.getVersion())) {
      return Arrays.asList(ImmutableEmptyCommand.builder()
          .bodyType(original.getBodyType())
          .id(newFormDocument.getId())
          .description(new ErrorMsgBuilder("Form document already exists, update not possible because of version conflict")
              .field("id", newFormDocument.getId())
              .field("existing version", oldDocument.getVersion())
              .field("new version", newFormDocument.getVersion())
              .build())
          .build());
    }
    
    final var body = this.client.getConfig().getMapper().toStoreBody(newFormDocument);
    return Arrays.asList(ImmutableUpdateStoreEntity.builder()
        .body(body)
        .bodyType(original.getBodyType())
        .id(newFormDocument.getId())
        .version(newFormDocument.getVersion())
        .build());
  }

  
  public List<StoreCommand> initFormRev(FormReleaseValueDocument original) {
    final var newRevDocument = this.client.getConfig().getMapper().readFormRevDoc(original.getCommands());
    final var oldDocument = state.getForms().get(newRevDocument.getId());
    
    if(oldDocument == null) {
      final var body = this.client.getConfig().getMapper().toStoreBody(newRevDocument);
      return Arrays.asList(ImmutableCreateStoreEntity.builder()
          .body(body)
          .bodyType(original.getBodyType())
          .id(newRevDocument.getId())
          .version(newRevDocument.getVersion())
          .build());
    }
    
    if(!oldDocument.getVersion().equals(newRevDocument.getVersion())) {
      this.skipOnForms.add(newRevDocument.getHead());
      newRevDocument.getEntries().forEach(e -> skipOnForms.add(e.getFormId()));
      
      return Arrays.asList(ImmutableEmptyCommand.builder()
          .bodyType(original.getBodyType())
          .id(newRevDocument.getId())
          .description(new ErrorMsgBuilder("Revision document already exists, update not possible because of version conflict")
              .field("id", newRevDocument.getId())
              .field("existing version", oldDocument.getVersion())
              .field("new version", newRevDocument.getVersion())
              .build())
          .build());
    }
    
    final var body = this.client.getConfig().getMapper().toStoreBody(newRevDocument);
    return Arrays.asList(ImmutableUpdateStoreEntity.builder()
        .body(body)
        .bodyType(original.getBodyType())
        .id(newRevDocument.getId())
        .version(newRevDocument.getVersion())
        .build());
  }
  
  private List<StoreCommand> initRelease(FormReleaseDocument original) {
    final var next = ImmutableFormReleaseDocument.builder().from(original).description("imported from a release").build();
    final var body = client.getConfig().getMapper().toStoreBody(next);
    return Arrays.asList(ImmutableCreateStoreEntity.builder().body(body).bodyType(original.getType()).build());
  }
  
  
  public static class ImportReleaseDocumentException extends RuntimeException implements DialobClientException {
    private static final long serialVersionUID = 7193990799948041231L;
    public ImportReleaseDocumentException(String message) {
      super(message);
    }
  }
}
