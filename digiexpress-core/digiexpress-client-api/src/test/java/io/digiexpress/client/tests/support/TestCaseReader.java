package io.digiexpress.client.tests.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.api.form.Form;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.ImmutableFormDocument;
import io.thestencil.client.api.CreateBuilder.BatchSite;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestCaseReader {
  private final ObjectMapper mapper;
  private final String src;
  private final LocalDateTime fixed = LocalDateTime.of(2022, 10, 15, 10, 21);
  
  public Form form(String file) {
    try {
      final var data = toString(file);
      return mapper.readValue(data, Form.class);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  public BatchSite content(String file) {
    try {
      final var data = toString(file);
      return mapper.readValue(data, BatchSite.class);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  public FormDocument formDocument(String file) {
    return ImmutableFormDocument.builder()
        .created(fixed)
        .updated(fixed)
        .data(form(file)).build();
  }
  
  
  private String toString(String resource) {
    try {
      final var type = TestCaseReader.class;
      return new String(type.getClassLoader().getResourceAsStream(src + resource).readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
