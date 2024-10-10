package io.dialob.client.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import io.dialob.api.form.Form;
import io.dialob.api.questionnaire.ContextValue;
import io.dialob.api.questionnaire.Questionnaire;
import io.smallrye.mutiny.Uni;

public interface DialobFill {
  FillBuilder create();
  FillQuery query();
  
  Uni<Questionnaire> save(Questionnaire questionnaire);
  
  interface FillBuilder {
    FillBuilder formId(String formId);
    FillBuilder language(String language);
    FillBuilder contextValues(Map<String, Object> contextValues);
    FillBuilder contextValues(Collection<ContextValue> contextValues);
    Uni<FillEntry> build();
  }
  
  interface FillQuery {
    FillQuery formId(String formId);
    Uni<Questionnaire> get(String id);
    Uni<List<Questionnaire>> find();
  }

  @Value.Immutable
  interface FillEntry extends Serializable {
    String getId();
    Questionnaire getQuestionnaire();
    Form getForm();
  }
}
