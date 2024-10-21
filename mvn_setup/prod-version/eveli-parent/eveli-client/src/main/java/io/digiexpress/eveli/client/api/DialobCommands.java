package io.digiexpress.eveli.client.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormTag;
import io.dialob.api.questionnaire.Answer;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.api.rest.IdAndRevision;
import lombok.Data;

public interface DialobCommands {
  
  QuestionnaireBuilder create();
  Questionnaire.Metadata.Status getStatus(String sessionId);
  Questionnaire get(String questionnaireId);
  Dialob getDialob(String questionnaireId);
  Form getForm(String formId);
  TagQueryBuilder getTags();
  
  void complete(String sessionId);  
  void delete(String sessionId);
  
  interface QuestionnaireBuilder {
    QuestionnaireBuilder formName(String formName);
    QuestionnaireBuilder formTag(String formTag);
    QuestionnaireBuilder language(String language);
    QuestionnaireBuilder addContext(String id, Serializable value);
    QuestionnaireBuilder addAnswer(String id, Serializable value);
    IdAndRevision build();
  }
  
  interface TagQueryBuilder {
    TagQueryBuilder formName(String formName);
    List<FormTag> build();
  }
  
  interface Dialob {
    Questionnaire unwrap();
    Answer answer(String name);
    String text(String name);
    BigDecimal decimal(String name);
    boolean bool(String name);
    Object variable(String name);
    Object context(String name);
    Questionnaire.Metadata metadata();
  }
  
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Data
  public static class FormListItem {
    private String id;
    private Form.Metadata metadata;
  }
  
  @Data
  public static class FormTagResult {
    private String formLabel;
    private String formName;
    private String tagFormId;
    private String tagName;
  }


}
