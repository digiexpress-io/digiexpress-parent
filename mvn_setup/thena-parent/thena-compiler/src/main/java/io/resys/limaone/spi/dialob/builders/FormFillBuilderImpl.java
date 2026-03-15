package io.resys.limaone.spi.dialob.builders;

import java.util.Objects;
import java.util.function.Consumer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.resys.limaone.spi.dialob.FormDb.FormFillBuilder;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.resys.limaone.spi.http.HttpClient.RawResponse;
import io.resys.limaone.spi.http.HttpClientImpl.RawResponseImpl;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FormFillBuilderImpl implements FormFillBuilder {
  private final FormDbProps db;
  
  private Consumer<Uni<?>> callback;
  private String formInstanceId;
  private String body;

  @Override
  public FormFillBuilder formInstanceId(String formInstanceId) {
    this.formInstanceId = Objects.requireNonNull(formInstanceId, () -> "formInstanceId can't be null");;
    return this;
  }
  @Override
  public FormFillBuilder actions(String body) {
    this.body = body;
    return this;
  }
  @Override
  public FormFillBuilder onCompletion(Consumer<Uni<?>> callback) {
    this.callback = Objects.requireNonNull(callback, () -> "callback can't be null");
    return this;
  }

  @Override
  public Uni<RawResponse> build() {
    Objects.requireNonNull(formInstanceId, () -> "formInstanceId can't be null");;
    if(isInvalidId(formInstanceId)) {
      return Uni.createFrom().item(new RawResponseImpl(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()));
    }
    
    return db.getQuestionnaireHttp().httpQuery()
      .uri(uri -> uri.append(formInstanceId).build())
      .method(String.class)
      .postOneAsRaw(body)
      .call(this::determineCallback);
  }
  
  private Uni<Void> determineCallback(RawResponse resp) {    
    if (callback != null) {
      callback.accept(Uni.createFrom().item(resp)); // or whatever Uni you want to pass
    }
    return Uni.createFrom().voidItem();
  }
  
  
  public static boolean isInvalidId(String sessionId) {
    return sessionId == null || !sessionId.matches("[a-fA-F0-9-_]+");
  }
}
