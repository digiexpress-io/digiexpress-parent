package io.resys.sysconfig.client.tests.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.api.form.Form;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.ImmutableFormDocument;
import io.resys.hdes.client.api.HdesComposer.CreateEntity;
import io.resys.hdes.client.api.ImmutableCreateEntity;
import io.resys.hdes.client.api.ast.AstBody;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
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
    final var data = form(file);
    return ImmutableFormDocument.builder()
        .id(data.getId())
        .version(data.getId())
        .created(fixed)
        .updated(fixed)
        .data(data).build();
  }
  
  public CreateEntity flow(String file) {
    final var body = toString(file);
    
    return ImmutableCreateEntity.builder()
        .type(AstBody.AstBodyType.FLOW)
        .addBody(ImmutableAstCommand.builder()
            .type(AstCommandValue.SET_BODY)
            .value(body)
            .build())
        .build();
  }  
  public CreateEntity flowService(String file) {
    final var body = toString(file);
    
    return ImmutableCreateEntity.builder()
        .type(AstBody.AstBodyType.FLOW_TASK)
        .addBody(ImmutableAstCommand.builder()
            .type(AstCommandValue.SET_BODY)
            .value(body)
            .build())
        .build();
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
