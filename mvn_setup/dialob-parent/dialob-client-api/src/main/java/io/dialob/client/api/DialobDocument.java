package io.dialob.client.api;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.api.form.Form;

// Entities that are body for StoreEntity.body
public interface DialobDocument extends Serializable {

  @Nullable String getId(); // unique id
  @Nullable String getVersion(); // not really nullable, just in serialization
  String getName();
  LocalDateTime getCreated();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableFormRevisionDocument.class) @JsonDeserialize(as = ImmutableFormRevisionDocument.class)
  interface FormRevisionDocument extends DialobDocument {
    String getHead(); //latest form id
    LocalDateTime getUpdated();    
    List<FormRevisionEntryDocument> getEntries();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableFormDocument.class) @JsonDeserialize(as = ImmutableFormDocument.class)
  interface FormDocument extends DialobDocument {
    Form getData();
    LocalDateTime getUpdated();
    
    @Value.Default
    default String getName() { return getData().getName(); }
  }

  @Value.Immutable @JsonSerialize(as = ImmutableFormRevisionEntryDocument.class) @JsonDeserialize(as = ImmutableFormRevisionEntryDocument.class)
  interface FormRevisionEntryDocument extends Serializable {
    String getRevisionName();
    String getFormId();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();
  }
}