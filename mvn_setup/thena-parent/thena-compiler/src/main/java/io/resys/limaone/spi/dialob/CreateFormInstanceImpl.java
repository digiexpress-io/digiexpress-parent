package io.resys.limaone.spi.dialob;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.dialob.api.rest.IdAndRevision;
import io.resys.limaone.spi.dialob.FormDb.CreateFormInstance;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateFormInstanceImpl implements CreateFormInstance {
  private final FormDbProps db;
  private String formId;
  private String language;
  private Map<String, Serializable> context = new HashMap<>();
  private Map<String, Serializable> answers = new HashMap<>();

  @Override
  public CreateFormInstance formId(String formId) {
    this.formId = Objects.requireNonNull(formId, () -> "formId must be defined");
    return this;
  }

  @Override
  public CreateFormInstance language(String language) {
    this.language = Objects.requireNonNull(language, () -> "language must be defined");
    return this;
  }

  @Override
  public CreateFormInstance context(Map<String, Serializable> ctx) {
    this.context = Objects.requireNonNull(ctx, () -> "context must be defined");
    return this;
  }

  @Override
  public CreateFormInstance answers(Map<String, Serializable> answers) {
    this.answers = Objects.requireNonNull(answers, () -> "answers must be defined");
    return this;
  }

  @Override
  public Uni<IdAndRevision> build() {
    Objects.requireNonNull(formId, () -> "formId must be defined");
    
    final var requestBody = JsonObject.mapFrom(createQuestionnaireBody());
    
    return db.getQuestionnaireHttp()
      .httpQuery()
      .uri(uri -> uri.append("questionnaires").build())
      .method(JsonObject.class)
      .postOneObject(requestBody, resp -> resp.mapTo(IdAndRevision.class));
  }
  
  private Map<String, Object> createQuestionnaireBody() {
    final var body = new HashMap<String, Object>();
    
    // Metadata section
    final var metadata = new HashMap<String, Object>();
    metadata.put("formId", formId);
    metadata.put("language", language != null ? language : "fi"); // default to Finnish like Spring impl
    body.put("metadata", metadata);
    
    // Context section (array of objects with id/value)
    final var contextArray = new java.util.ArrayList<Map<String, String>>();
    for (final var entry : context.entrySet()) {
      if (entry.getValue() != null) {
        final var contextItem = new HashMap<String, String>();
        contextItem.put("id", entry.getKey());
        contextItem.put("value", String.valueOf(entry.getValue()));
        contextArray.add(contextItem);
      }
    }
    body.put("context", contextArray);
    
    // Answers section (array of objects with id/value)
    final var answersArray = new java.util.ArrayList<Map<String, String>>();
    for (final var entry : answers.entrySet()) {
      if (entry.getValue() != null) {
        final var answerItem = new HashMap<String, String>();
        answerItem.put("id", entry.getKey());
        answerItem.put("value", String.valueOf(entry.getValue()));
        answersArray.add(answerItem);
      }
    }
    body.put("answers", answersArray);
    
    return body;
  }
}
