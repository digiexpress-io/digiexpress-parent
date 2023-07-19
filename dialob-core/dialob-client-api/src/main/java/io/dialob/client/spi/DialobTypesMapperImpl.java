package io.dialob.client.spi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.api.form.Form;
import io.dialob.api.form.ImmutableForm;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormReleaseDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.dialob.client.api.DialobErrorHandler.DialobClientException;
import io.dialob.client.api.DialobStore.StoreEntity;
import io.dialob.client.api.ImmutableFormDocument;
import io.dialob.client.api.ImmutableFormReleaseDocument;
import io.dialob.client.api.ImmutableFormRevisionDocument;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DialobTypesMapperImpl implements DialobClient.TypesMapper {
  private final ObjectMapper objectMapper; 


  @Override
  public String toJson(Object anyObject) {
    return doInMapper(om -> om.writeValueAsString(anyObject));
  }
  @Override
  public FormDocument readFormDoc(String entity) {
    return doInMapper(om -> om.readValue(entity, FormDocument.class));
  }
  @Override
  public Questionnaire readQuestionnaire(InputStream entity) {
    return doInMapper(om -> om.readValue(entity, Questionnaire.class));
  }
  @Override
  public String toString(InputStream entity) {
    return doInMapper(om -> new String(entity.readAllBytes(), StandardCharsets.UTF_8));
  }
  @Override
  public String toStoreBody(FormRevisionDocument anyObject) {
    return doInMapper(om -> om.writeValueAsString(
        ImmutableFormRevisionDocument.builder()
          .from(anyObject)
          .version(null)
          .id(null)
          .build()));
  }
  @Override
  public String toStoreBody(FormDocument anyObject) {
    return doInMapper(om -> om.writeValueAsString(ImmutableForm.builder()
          .from(anyObject.getData())
          .rev(null).id(null)
          .build()));
  }
  @Override
  public String toStoreBody(FormReleaseDocument anyObject) {
    return doInMapper(om -> om.writeValueAsString(ImmutableFormReleaseDocument.builder()
          .from(anyObject)
          .version(null).id(null)
          .build()));
  }
  @Override
  public Form readForm(String entity) {
    return doInMapper(om -> om.readValue(entity, Form.class));
  }
  @Override
  public FormReleaseDocument readReleaseDoc(String entity) {
    return doInMapper(om -> om.readValue(entity, FormReleaseDocument.class));
  }
  @Override
  public FormRevisionDocument readFormRevDoc(String entity) {
    return doInMapper(om -> om.readValue(entity, FormRevisionDocument.class));
  }
  @Override
  public FormReleaseDocument toFormReleaseDoc(StoreEntity store) {
    return doInMapper(om -> {
      final var from = om.readValue(store.getBody(), FormReleaseDocument.class);
      return ImmutableFormReleaseDocument.builder().from(from)
          .version(store.getId())
          .id(store.getVersion())
          .build();
    });
  }
  @Override
  public FormRevisionDocument toFormRevDoc(StoreEntity store) {
    return doInMapper(om -> {
      final var from = om.readValue(store.getBody(), FormRevisionDocument.class);
      return ImmutableFormRevisionDocument.builder().from(from)
          .version(store.getVersion())
          .id(store.getId())
          .build();
    });
  }
  @Override
  public FormDocument toFormDoc(StoreEntity store) {
    return doInMapper(om -> {
      final var data = ImmutableForm.builder()
          .from(om.readValue(store.getBody(), Form.class))
          .id(store.getId())
          .rev(store.getVersion())
          .build();
      
      return ImmutableFormDocument.builder()
          .version(store.getVersion())
          .id(store.getId())
          .name(data.getName() == null ? data.getId() : data.getName())
          .created(data.getMetadata().getCreated() == null ?  LocalDateTime.now() : LocalDateTime.ofInstant(data.getMetadata().getCreated().toInstant(), ZoneId.systemDefault()))
          .updated(data.getMetadata().getLastSaved() == null ?  LocalDateTime.now() : LocalDateTime.ofInstant(data.getMetadata().getLastSaved().toInstant(), ZoneId.systemDefault()))
          .data(data)
          .build();
    });
  }
  
  public <T> T doInMapper(MapperFunction<T> input) {
    try {
      return input.apply(objectMapper);
    } catch(IOException e) {
      throw new DialobJsonException(e.getMessage(), e);      
    }
  }
  
  @FunctionalInterface
  private interface MapperFunction<T> {
    T apply(ObjectMapper om) throws IOException;
  }
  
  
  public static class DialobJsonException extends RuntimeException implements DialobClientException {
    private static final long serialVersionUID = -7154685569622201632L;
    public DialobJsonException(String message, Throwable cause) {
      super(message, cause);
    }
    public DialobJsonException(String message) {
      super(message);
    }
  }

}
