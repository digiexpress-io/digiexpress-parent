package io.digiexpress.client.tests.migration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.dialob.api.form.Form;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.digiexpress.client.tests.migration.DialobMigrationVisitor.DialobFormMeta;
import io.digiexpress.client.tests.migration.DialobMigrationVisitor.DialobFormTag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@lombok.Builder
public class DialobMigration {
  @lombok.Builder.Default
  private final String src = MigrationsDefaults.folder + "dialob";
  @lombok.Builder.Default
  private final ObjectMapper om = MigrationsDefaults.om;
  
  @lombok.Data @lombok.Builder
  public static class FormsAndRevs {
    private List<FormRevisionDocument> revs;
    private List<FormDocument> forms;
  }
  
  public FormsAndRevs execute() {
    final var dir = new File(src);
    ServiceAssert.isTrue(dir.isDirectory() && dir.canRead() && dir.exists(), () -> src + " must be a directory with dialob *form.json-s");
    final var files = dir.listFiles((file, name) -> name.endsWith(".json"));
    
    final var summary = MigrationsDefaults.summary("file name", "form name", "status", "type");
    final var forms = new ArrayList<Form>();
    for(final var file : files) {
      if(file.getName().equals("allforms.json") || file.getName().equals("alltags.json")) {
        continue;
      }
      try {
        final var form = om.readValue(file, Form.class);
        forms.add(form);
        summary.addRow(file.getName(), form.getName(), "OK", "form");
      } catch (Exception e) {
        summary.addRow(file.getName(), null, "FAIL", "form");
        throw new RuntimeException("Failed to read dialob form json: " + file.getName() + e.getMessage(), e);
      }
    }
    
    final var allFormsJson = new File(dir, "allforms.json");
    final var allFormMetas = new ArrayList<DialobFormMeta>();
    try {
      final var allmeta = om.readValue(allFormsJson, DialobFormMeta[].class);
      allFormMetas.addAll(Arrays.asList(allmeta));
      summary.addRow(allFormsJson.getName(), allFormsJson.getName(), "OK", "meta");
    } catch (Exception e) {
      summary.addRow(allFormsJson.getName(), null, "FAIL", "meta");
      log.error(summary.toString());
      throw new RuntimeException("Failed to read dialob form json: " + allFormsJson.getName() + e.getMessage(), e);
    }
    final var allTags = new ArrayList<DialobFormTag>();
    final var allTagsJson = new File(dir, "alltags.json");
    try {
      
      for(final var value : om.readValue(new File(dir, "alltags.json"), ArrayNode.class)) {
        final var tags = om.convertValue(value, DialobFormTag[].class);
        allTags.addAll(Arrays.asList(tags));
      }
      summary.addRow(allTagsJson.getName(), allTagsJson.getName(), "OK", "tags");
    } catch (Exception e) {
      summary.addRow(allTagsJson.getName(), null, "FAIL", "tag");
      log.error(summary.toString());
      throw new RuntimeException("Failed to read dialob form json: " + allTagsJson.getName() + e.getMessage(), e);
    }
  
    log.info("Reading dialob forms from: '" + src + "', found: " + files.length + summary.toString());
    final var visitor = new DialobMigrationVisitor();
    allFormMetas.forEach(meta -> visitor.visitFormRev(meta));
    forms.forEach(form -> visitor.visitForm(form));
    allTags.forEach(tag -> visitor.visitTag(tag));
    return visitor.build();
  }
  
  
}
