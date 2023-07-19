package io.dialob.client.spi.composer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;

import io.dialob.client.api.DialobClientConfig;
import io.dialob.client.api.DialobComposer.ComposerReleaseState;
import io.dialob.client.api.DialobComposer.ComposerState;
import io.dialob.client.api.DialobComposer.CreateComposerRelease;
import io.dialob.client.api.DialobDocument.DocumentType;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormReleaseValueDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.dialob.client.api.DialobStore.CreateStoreEntity;
import io.dialob.client.api.ImmutableComposerReleaseState;
import io.dialob.client.api.ImmutableCreateStoreEntity;
import io.dialob.client.api.ImmutableFormReleaseDocument;
import io.dialob.client.api.ImmutableFormReleaseValueDocument;
import io.dialob.client.spi.migration.MigrationSupport;
import io.dialob.client.spi.migration.MigrationSupport.Migration;
import io.dialob.client.spi.support.Sha2;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateReleaseVisitor {

  private final DialobClientConfig config;
  private final ComposerState state;
  private final StringBuilder log = new StringBuilder();

  @Data @Builder
  public static class Result {
    private CreateStoreEntity storeEntity;
    private ComposerReleaseState releaseState;
  }
  
  public Result visit(CreateComposerRelease command) {
    final var values = new ArrayList<FormReleaseValueDocument>();
        
    for(final var document : state.getForms().values()) {
      visitLog(document);
      final var commands = config.getMapper().toJson(document);
      values.add(ImmutableFormReleaseValueDocument.builder()
          .bodyType(DocumentType.FORM)
          .commands(commands)
          .hash(Sha2.blob(commands))
          .build());
    }
    
    for(final var revision : state.getRevs().values()) {
      visitLog(revision);
      final var commands = config.getMapper().toJson(revision);
      values.add(ImmutableFormReleaseValueDocument.builder()
          .bodyType(DocumentType.FORM_REV)
          .commands(commands)
          .hash(Sha2.blob(commands))
          .build());
    }
    
    final var rel = ImmutableFormReleaseDocument.builder()
        .name(command.getName())
        .description(command.getDescription())
        .created(LocalDateTime.now())
        .addAllValues(values)
        .build();
    
    final var compress = Migration.builder().log(log.toString()).release(rel).build();
    final var mig = visitMigration(compress);
    
    final var storeEntity = ImmutableCreateStoreEntity.builder()
        .bodyType(DocumentType.RELEASE)
        .body(config.getMapper().toStoreBody(rel))
        .build();
    
    final var releaseState = ImmutableComposerReleaseState.builder()
        .description(command.getDescription())
        .name(command.getName())
        .content(mig)
        .hash(compress.getHash())
        .id("")
        .build();
    
    return Result.builder().storeEntity(storeEntity).releaseState(releaseState).build();
  }

  private void visitLog(FormDocument rev) {
    
  }
  
  private void visitLog(FormRevisionDocument rev) {
    log.append(System.lineSeparator())
    .append("  - form id: ").append(rev.getId()).append(System.lineSeparator())
    .append("  - form name: '").append(rev.getName()).append("'").append(System.lineSeparator())
    .append("  - form desc: '").append(rev.getDescription()).append("'").append(System.lineSeparator())
    .append("  - form created: ").append(rev.getCreated()).append(System.lineSeparator())
    .append("  - form head: ").append(rev.getHead()).append(System.lineSeparator());
    
    for(final var entry : rev.getEntries()) {
      log.append("    - ").append(entry.getRevisionName()).append(": ").append(entry.getFormId())
      .append(System.lineSeparator());
    }
  }
  
  private String visitMigration(Migration mig) {
    ByteArrayOutputStream out = null;
    try {
      out = new ByteArrayOutputStream();
      new MigrationSupport(config.getMapper()).write(mig, out);

      return new String(out.toByteArray(), StandardCharsets.UTF_8);
    } finally {      
      if(out != null) {
        try { out.close(); } catch(Exception e) {}
      }
    }      
  }
  
}
