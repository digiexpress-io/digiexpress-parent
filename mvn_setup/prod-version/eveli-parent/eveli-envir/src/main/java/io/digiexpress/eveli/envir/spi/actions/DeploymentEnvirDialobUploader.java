package io.digiexpress.eveli.envir.spi.actions;

import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.dialob.api.form.Form;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.thestencil.client.api.MigrationBuilder.LocalizedSite;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.api.MigrationBuilder.TopicLink;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DeploymentEnvirDialobUploader {

  private final DialobClient dialobClient;
  private final EveliDeployment deployment;
  private final Sites sites;
  private final JsonObject errors = JsonObject.of();
  private int errorIndex;
  
  public Optional<JsonObject> accept() {
    visitSites(sites);
    
    if(errors.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(errors);
  }

  private void visitSites(Sites sites) {
    sites.getSites().forEach(this::visitSite);
  }
  
  private void visitSite(String locale, LocalizedSite site) {
    site.getLinks().values().stream().forEach(link -> visitTopicLink(locale, link)); 
  }
  
  private void visitTopicLink(String locale, TopicLink link) {
    if(!Boolean.TRUE.equals(link.getWorkflow())) {
      return;
    }
    visitForm(locale, link);
  }
  
  private void visitForm(String locale, TopicLink link) {
    final var form = deployment.getSources().getDialob().stream()
        .filter(e -> link.getFormId().equals(e.getId()))
        .findFirst()
        .get();

    final var tag = dialobClient.findAllFormTags(form.getName()).stream()
        .filter(e -> e.getName().equals(link.getFormTag()))
        .findFirst();
    
    final var existingForm = dialobClient.findOneFormById(link.getFormId());
    
    
    // no tag and no form
    if(tag.isEmpty() && existingForm.isEmpty()) {
      final var created = createForm(form, link);
      if(created != null) {
        createTag(created, link);
      }
      return;
    }
    
    // no tag but form is present with same revision
    if(tag.isEmpty() && existingForm.isPresent() && existingForm.get().getRev().equals(form.getRev())) {
      createTag(form, link);
      return;
    }
    
    
    // no tag but form has different revision
    if(tag.isEmpty() && existingForm.isPresent() && !existingForm.get().getRev().equals(form.getRev())) {
      final var updated = updateForm(form, link);
      createTag(updated, link);
      return;
    }
  }
  
  
  private Form updateForm(Form form, TopicLink link) {
    try {
      return dialobClient.updateForm(form);
    } catch(Exception e) {
      final var stack = String.join(System.lineSeparator(), ExceptionUtils.getRootCauseStackTrace(e));
      addError(
          "FAILED_TO_UPDATE_FORM", 
          "Can't create dialob form via dialob api for workflow: " + JsonObject.mapFrom(link).encodePrettily() + System.lineSeparator() +
          "because of error in rest api: " + e.getMessage() + System.lineSeparator() +
          "stack: " + System.lineSeparator() +
          stack
      );
      return null;
    }
  }
  
  private Form createForm(Form form, TopicLink link) {
    try {
      return dialobClient.createForm(form);
    } catch(Exception e) {
      final var stack = String.join(System.lineSeparator(), ExceptionUtils.getRootCauseStackTrace(e));
      addError(
          "FAILED_TO_CREATE_FORM", 
          "Can't create dialob form via dialob api for workflow: " + JsonObject.mapFrom(link).encodePrettily() + System.lineSeparator() +
          "because of error in rest api: " + e.getMessage() + System.lineSeparator() +
          "stack: " + System.lineSeparator() +
          stack
      );
      
      return null;
    }
  }
  
  private void createTag(Form form, TopicLink link) {
    try {
      dialobClient.createTag(form.getName(), link.getFormTag());
    } catch(Exception e) {
      final var stack = String.join(System.lineSeparator(), ExceptionUtils.getRootCauseStackTrace(e));
      addError(
          "FAILED_TO_CREATE_FORM_TAG", 
          "Can't create dialob form via dialob api for workflow: " + JsonObject.mapFrom(link).encodePrettily() + System.lineSeparator() +
          "because of error in rest api: " + e.getMessage() + System.lineSeparator() +
          "stack: " + System.lineSeparator() +
          stack
      );
    }
  }
  
  private void addError(String code, String error) {
    errorIndex++;
    errors.put(errorIndex + "-" + code, error);
  }
}
