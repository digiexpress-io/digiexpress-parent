package io.dialob.client.pgsql.migration;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.dialob.api.form.Form;
import io.dialob.api.form.ImmutableForm;
import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobDocument.DocumentType;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormReleaseValueDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.dialob.client.api.DialobErrorHandler.DocumentNotFoundException;
import io.dialob.client.api.ImmutableFormDocument;
import io.dialob.client.api.ImmutableFormReleaseDocument;
import io.dialob.client.api.ImmutableFormReleaseValueDocument;
import io.dialob.client.api.ImmutableFormRevisionDocument;
import io.dialob.client.api.ImmutableFormRevisionEntryDocument;
import io.dialob.client.spi.exceptions.ErrorMsgBuilder;
import io.dialob.client.spi.migration.MigrationSupport.Migration;
import io.dialob.client.spi.support.OidUtils;
import io.dialob.client.spi.support.Sha2;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class MigrationVisitor {

  private final DialobClient.TypesMapper mapper;
  private final Map<String, FormRevisionDocument> revisions = new LinkedHashMap<>();
  private final Collection<String> unresolvedFormDocuments = new HashSet<>();
  private final Map<String, FormDocumentWrapper> resolvedFormDocuments = new HashMap<>();
  private final Map<String, FormDocumentWrapper> resolvedWithMissingRevs = new HashMap<>();
  private final Map<String, FormDocumentWrapper> resolvedWithConflictingIds = new HashMap<>();  
  
  @Data
  @Builder
  private static class FormDocumentWrapper {
    final String id;
    final String version;
    final String data;
    final LocalDateTime created;
    final LocalDateTime updated;
  }
  
  public void visitFormRev(io.vertx.mutiny.sqlclient.Row row) {
    final var form_name = row.getString("form_name");
    final var name = row.getString("name");
    final var form_document_id = row.getUUID("form_document_id").toString();
    final var description = row.getString("description");
    final var created = row.getLocalDateTime("created");
    final var updated = row.getLocalDateTime("updated");
    

    final var entry = ImmutableFormRevisionEntryDocument.builder()
        .id(OidUtils.gen())
        .formId(form_document_id)
        .description(description)
        .revisionName(name)
        .created(created)
        .updated(updated)
        .build();
    
    
    if(!revisions.containsKey(form_name)) {
      throw new DocumentNotFoundException(
          new ErrorMsgBuilder("Revision document not found!")
          .field("form_name", form_name)
          .field("name", name)
          .build());
    }
    
    final var revision = ImmutableFormRevisionDocument.builder().from(revisions.get(form_name));
    revisions.put(form_name, revision.addEntries(entry).build());
    unresolvedFormDocuments.add(form_document_id);
  }
  
  public void visitForm(io.vertx.mutiny.sqlclient.Row row) {
  
    final var label = row.getString("label");
    final var name = row.getString("name");
    final var latest_form_id = row.getUUID("latest_form_id").toString();
    final var created = row.getLocalDateTime("created");
    final var updated = row.getLocalDateTime("updated");
    
    final var revision = ImmutableFormRevisionDocument.builder()
        .id(name)
        .name(label)
        .created(created)
        .updated(updated)
        .head(latest_form_id)
        .build();
    
    unresolvedFormDocuments.add(latest_form_id);
    revisions.put(name, revision);
  }
  
  public void visitFormDocument(io.vertx.mutiny.sqlclient.Row row) {
    
    final var id = row.getUUID("id").toString();
    final var rev = row.getInteger("rev");
    final var data = row.getString("data");
    final var created = row.getLocalDateTime("created");
    final var updated = row.getLocalDateTime("updated");
        
    final var form = ImmutableForm.builder().from(mapper.readForm(data))
        .id(null).rev(null)
        .build();
    
    final var document = FormDocumentWrapper.builder()
        .id(id)
        .version(rev.toString())
        .created(created)
        .updated(updated)
        .data(mapper.toJson(form))
        .build();
    
    if(!unresolvedFormDocuments.contains(id)) {
      resolvedWithMissingRevs.put(id, document);
    } else if(resolvedFormDocuments.containsKey(id)) {
      resolvedWithConflictingIds.put(id, document);
    } else {
      resolvedFormDocuments.put(id, document);
    }
  }
  
  private final Form clean(String data) {
    final var form = mapper.readForm(data);
    return ImmutableForm.builder().from(form)
        .id(null).rev(null)
        .build();
  }
  
  
  private void createMissing() {
    for(final var wrapper : this.resolvedWithMissingRevs.values()) {

      final var data = clean(wrapper.getData());
      final var revision = ImmutableFormRevisionDocument.builder()
          .id(OidUtils.gen())
          .name(data.getMetadata().getLabel())
          .created(LocalDateTime.ofInstant(data.getMetadata().getCreated().toInstant(), ZoneId.systemDefault()))
          .updated(LocalDateTime.ofInstant(data.getMetadata().getLastSaved().toInstant(), ZoneId.systemDefault()))
          .head(wrapper.getId())
          .description("MISSING_FORM_REV")
          .build();
      revisions.put(revision.getId(), revision);
      resolvedFormDocuments.put(wrapper.getId(), wrapper);
    }
    
  }
  
  public Migration build() {
    createMissing();
    final var values = new ArrayList<FormReleaseValueDocument>();
    
    List<FormDocument> docs = new ArrayList<>();    
    for(final var wrapper : this.resolvedFormDocuments.values()) {
      final var document = ImmutableFormDocument.builder()
          .id(wrapper.getId())
          .version(wrapper.getVersion())
          .created(wrapper.getCreated())
          .updated(wrapper.getUpdated())
          .data(clean(wrapper.getData()))
          .build();
      docs.add(document);
      
      final var commands = mapper.toJson(document);
      values.add(ImmutableFormReleaseValueDocument.builder()
          .bodyType(DocumentType.FORM)
          .commands(commands)
          .hash(Sha2.blob(commands))
          .build());
    }
    
    for(final var revision : this.revisions.values()) {
      final var commands = mapper.toJson(revision);
      values.add(ImmutableFormReleaseValueDocument.builder()
          .bodyType(DocumentType.FORM_REV)
          .commands(commands)
          .hash(Sha2.blob(commands))
          .build());
    }
    
    unresolvedFormDocuments.removeAll(resolvedFormDocuments.keySet());
    
    if(!unresolvedFormDocuments.isEmpty()) {
      throw new DocumentNotFoundException(
          new ErrorMsgBuilder("Form revision data not found!")
          .field("form id-s:", String.join(", ", unresolvedFormDocuments))
          .build());
    }
    final var log = log();
    
    final var rel = ImmutableFormReleaseDocument.builder()
        .name("migration")
        .created(LocalDateTime.now())
        .addAllValues(values)
        .build();
    
    return Migration.builder()
        .log(log)
        .release(rel)
        .build(); 
  }
  
  private String log() {

    final var log = new StringBuilder("Found " + revisions.keySet().size() + " revisions: ")
        .append(", with missing revision: ").append(this.resolvedWithMissingRevs.keySet().size())
        .append(System.lineSeparator());
    
    for(final var rev : this.revisions.values()) {
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

    log.append(System.lineSeparator()).append("Conflicting id: " + this.resolvedWithConflictingIds.keySet().size()).append(System.lineSeparator());   
    for(final var conflict : resolvedWithConflictingIds.values()) {
      log.append(
        new ErrorMsgBuilder("Form document id already in use!")
        .field("id", conflict.getId())
        .field("name", conflict.getData())
        .build());
    }
    
    return log.toString();
  }
}
