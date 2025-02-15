package io.digiexpress.mig.client.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.vertx.core.json.JsonObject;


@Value.Immutable
public interface SourceForms {
  List<SourceForm> getForms();
  List<SourceFormDocument> getFormDocument();
  List<SourceFormRev> getFormRev();
  List<SourceQuestionnaire> getQuestionnaires();

  
  @Value.Immutable
  interface SourceForm {
    String getName();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();
    String getLatest_form_id();
    String getTenant_id();
    
    Optional<String> getLabel();
  }
  @Value.Immutable
  interface SourceFormDocument {
    String getId();
    int getRev();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();
    Optional<JsonObject> getData();
    String getTenant_id();
  }
  @Value.Immutable
  interface SourceFormRev {
    String getForm_name();
    String getName();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();
    String getForm_document_id();
    String getTenant_id();
    
    Optional<String> getDescription();
    Optional<String> getType();
    Optional<String> getRef_name();
  }
  
  @Value.Immutable
  interface SourceQuestionnaire {
    String getId();
    int getRev();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();
    String getForm_document_id();
    String getTenant_id();
    
    Optional<JsonObject> getData();
    Optional<String> getStatus();
    Optional<String> getOwner();
  }
}