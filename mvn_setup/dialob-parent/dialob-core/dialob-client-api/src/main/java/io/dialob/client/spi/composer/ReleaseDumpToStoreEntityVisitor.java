package io.dialob.client.spi.composer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.client.api.DialobDocument.DocumentType;
import io.dialob.client.api.DialobDocument.FormReleaseValueDocument;
import io.dialob.client.api.DialobErrorHandler.DialobClientException;
import io.dialob.client.api.DialobStore.StoreEntity;
import io.dialob.client.api.ImmutableFormReleaseDocument;
import io.dialob.client.api.ImmutableStoreEntity;
import io.dialob.client.spi.DialobTypesMapperImpl;
import io.dialob.client.spi.migration.MigrationSupport;
import io.dialob.client.spi.migration.MigrationSupport.MigrationContent;

public class ReleaseDumpToStoreEntityVisitor {

  private final DialobTypesMapperImpl mapper;
  private final MigrationContent release;
  private final List<String> skipOnForms = new ArrayList<>();

  public ReleaseDumpToStoreEntityVisitor(Resource text, ObjectMapper om) {
    this.mapper = new DialobTypesMapperImpl(om);
    this.release = readDumpRelease(text);
  }

  private MigrationContent readDumpRelease(Resource text) {
    try {
      
      final var reader = new MigrationSupport(mapper);
      return reader.read(text.getInputStream());
    } catch (Exception e) {
      throw new ReleaseDumpToStoreEntityException("Failed to load asset from: " + text.getFilename() + "!" + e.getMessage(), e);
    }
  }
  
  public void visit(Consumer<StoreEntity> consumer) {
    
    release.getRelease().getValues().stream()
      .filter(e -> e.getBodyType() == DocumentType.FORM_REV)
      .map(this::visitBody)
      .flatMap(e -> e.stream())
      .forEach(consumer);
    
    release.getRelease().getValues().stream()
        .filter(e -> e.getBodyType() == DocumentType.FORM)
        .map(this::visitBody)
        .flatMap(e -> e.stream())    
        .forEach(consumer);
    initRelease().forEach(consumer);
  }
  
  private List<StoreEntity> visitBody(FormReleaseValueDocument original) {
    switch (original.getBodyType()) {
    case FORM: return initForm(original);
    case FORM_REV: return initFormRev(original);
    case RELEASE: return Collections.emptyList();
    default: throw new ReleaseDumpToStoreEntityException("Unknown asset: '" + original.getBodyType() + "'!"); 
    }
  }

  public List<StoreEntity> initForm(FormReleaseValueDocument original) {
    
    final var newFormDocument = mapper.readFormDoc(original.getCommands());
    if(skipOnForms.contains(newFormDocument.getId())) {
      return Collections.emptyList();
    }

    final var body = mapper.toStoreBody(newFormDocument);
    return Arrays.asList(ImmutableStoreEntity.builder()
        .body(body)
        .bodyType(original.getBodyType())
        .id(newFormDocument.getId())
        .version(newFormDocument.getVersion())
        .build());
  }

  
  public List<StoreEntity> initFormRev(FormReleaseValueDocument original) {
    final var newRevDocument = mapper.readFormRevDoc(original.getCommands());
    final var body = mapper.toStoreBody(newRevDocument);
    return Arrays.asList(ImmutableStoreEntity.builder()
        .body(body)
        .bodyType(original.getBodyType())
        .id(newRevDocument.getId())
        .version(newRevDocument.getVersion() == null ? "undefined": newRevDocument.getVersion())
        .build());
  }
  
  private List<StoreEntity> initRelease() {
    final var original = release.getRelease(); 
    final var next = ImmutableFormReleaseDocument.builder().from(original).description("imported from a release").build();
    final var body = mapper.toStoreBody(next);
    return Arrays.asList(ImmutableStoreEntity.builder()
        .id(original.getName())
        .version("")
        .body(body)
        .bodyType(original.getType())
        .build());
  }
  
  
  public static class ReleaseDumpToStoreEntityException extends RuntimeException implements DialobClientException {
    private static final long serialVersionUID = 7193990799948041231L;
    public ReleaseDumpToStoreEntityException(String message) {
      super(message);
    }
    public ReleaseDumpToStoreEntityException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
