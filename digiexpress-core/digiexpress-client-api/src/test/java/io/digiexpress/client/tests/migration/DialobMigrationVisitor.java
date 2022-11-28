package io.digiexpress.client.tests.migration;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import io.dialob.api.form.Form;
import io.dialob.api.form.ImmutableForm;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.dialob.client.api.DialobDocument.FormRevisionEntryDocument;
import io.dialob.client.api.ImmutableFormDocument;
import io.dialob.client.api.ImmutableFormRevisionDocument;
import io.dialob.client.api.ImmutableFormRevisionEntryDocument;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.digiexpress.client.tests.migration.DialobMigration.FormsAndRevs;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class DialobMigrationVisitor {

  private final Map<String, FormRevisionDocument> revisions = new LinkedHashMap<>();
  private final Map<String, FormDocument> forms = new LinkedHashMap<>();
  private final Map<String, String> formId_to_tag = new LinkedHashMap<>();
  
  private final Function<Date, LocalDateTime> toDateTime = (dateToConvert) -> Instant.ofEpochMilli(dateToConvert.getTime())
    .atZone(ZoneId.systemDefault())
    .toLocalDateTime();

  @Data
  @Builder
  private static class FormDocumentWrapper {
    final String id;
    final String version;
    final String data;
    final LocalDateTime created;
    final LocalDateTime updated;
  }

  @Data
  @Builder
  @Jacksonized
  public static class DialobFormMeta {
    private String id;
    private DialobFormMetadata metadata;
  }
  
  @Data
  @Builder
  @Jacksonized
  public static class DialobFormMetadata {
    private String label;
    private Date created;
    private Date lastSaved;
    private String tenantId;
  }
  
  @Data
  @Builder
  @Jacksonized
  public static class DialobFormTag {
    private String name; // tag name
    private String formName; // tag name Form.formName - group name
    private String formId;
    private String description;
    private String type;
    private Date created;
    private String tenantId;
  }
  
  public void visitFormRev(DialobFormMeta src) {
    ServiceAssert.isTrue(!revisions.containsKey(src.getId()), () -> "Can't redefine form revision id: " + src.getId());
    final var created = src.getMetadata().getCreated();
    final var updated = src.getMetadata().getLastSaved();
    final var revision = ImmutableFormRevisionDocument.builder()
        .id(src.getId())
        .name(src.getMetadata().getLabel())
        .description("")
        .head("main")
        .created(toDateTime.apply(created))
        .updated(toDateTime.apply(updated))
        .build();
    revisions.put(src.getId(), revision);
  }
  
  public void visitTag(DialobFormTag src) {
    ServiceAssert.isTrue(revisions.containsKey(src.getFormName()), () -> "Can't find form revision id: " + src.getFormName());
    final var created = toDateTime.apply(src.getCreated());

    if(src.getName() == null) {
      log.warn("No name for tag in form: " + src.getFormName());
    }
    
    final var entry = ImmutableFormRevisionEntryDocument.builder()
      .id(null)
      .formId(src.getFormId())
      .description(src.getDescription())
      .revisionName(src.getName() == null ? "" : src.getName())
      .created(created)
      .updated(created)
      .build();
    final var revision = ImmutableFormRevisionDocument.builder().from(revisions.get(src.getFormName()))
        .addEntries(entry)
        .build();
    revisions.put(revision.getId(), revision);
    formId_to_tag.put(src.getFormId(), entry.getRevisionName());
  }
  
  public void visitForm(Form src) {
    
    final var id = src.getId();
    final var rev = src.getRev();
    final var created = src.getMetadata().getCreated();
    final var updated = src.getMetadata().getLastSaved();
    
    ServiceAssert.isTrue(!forms.containsKey(src.getId()), () -> "Can't redefine form id: " + src.getId());
    
    final var form = ImmutableForm.builder().from(src)
        .id(null).rev(null)
        .build();
    
    final var document = ImmutableFormDocument.builder()
        .id(id)
        .version(rev)
        .created(toDateTime.apply(created))
        .updated(toDateTime.apply(updated))
        .data(form)
        .build();
    
    this.forms.put(document.getId(), document);
  }

  
  
  public FormsAndRevs build() {
    for(final var form : this.forms.values()) {
      if(!formId_to_tag.containsKey(form.getId())) {
        log.warn("form id: " + form.getId() + " not on any tag");
      }
    }
    final var cleanedRevs = new ArrayList<FormRevisionDocument>();
    final var cleanedForms = new ArrayList<FormDocument>();
    final var summary = MigrationsDefaults.summary("group name", "tag name", "created", "form id", "form status");
    for(final var rev : revisions.values()) {
      final var entries = new ArrayList<FormRevisionEntryDocument>();
      
      for(final var entry : rev.getEntries()) {
        final var status = forms.containsKey(entry.getFormId()) ? "OK" : "MISSING";
        summary.addRow(rev.getId(), entry.getRevisionName(), entry.getCreated(), entry.getFormId(), status);
        if(status.equals("OK")) {
          entries.add(entry);
          cleanedForms.add(forms.get(entry.getFormId()));
        }
      }
      
      cleanedRevs.add(ImmutableFormRevisionDocument.builder()
          .from(rev)
          .entries(entries)
          .build());
    }
    
    log.info("Create revisions" + summary.toString());
    return FormsAndRevs.builder().revs(cleanedRevs).forms(cleanedForms).build();
  }
}
