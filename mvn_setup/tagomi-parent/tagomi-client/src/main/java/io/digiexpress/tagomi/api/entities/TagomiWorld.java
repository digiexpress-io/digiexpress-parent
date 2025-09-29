package io.digiexpress.tagomi.api.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

// world state after compilation 
public interface TagomiWorld {
  Map<String, List<TagomiProgram>> getProgramsByName();

  interface PdfCompiler {
    PdfCompiler inputField(String name, Serializable value);
    PdfCompiler inputMap(Map<String, Serializable> input);
    PdfCompiler inputEntity(Object inputObject);
    PdfCompiler inputList(List<Object> inputObject);
    PdfCompiler inputJson(JsonNode json);
    PdfCompiler locale(String locale);
    
    Uni<PdfEnvelope> compile(String programIdOrName);
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutablePdfEnvelope.class)
  @JsonDeserialize(as = ImmutablePdfEnvelope.class)
  interface PdfEnvelope {
    TagomiPdfStatus getStatus();
    @Nullable Pdf getValue();
  }
  
  interface Pdf {
    String getName();
    String getLocale();
    byte[] getBody();
  }
  
  enum TagomiPdfStatus {
    OK, ERROR
  }
}
