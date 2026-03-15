package io.resys.limaone.spi.dialob.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormItem;
import io.dialob.api.form.FormValueSet;
import io.dialob.api.form.FormValueSetEntry;
import io.dialob.api.questionnaire.Answer;
import io.dialob.api.questionnaire.ContextValue;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.api.questionnaire.VariableValue;
import io.resys.limaone.spi.dialob.FormDb.FormInstance;
import io.resys.limaone.spi.dialob.builders.FormInstanceQueryImpl;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class FormInstanceImpl implements FormInstance {
  private final Questionnaire questionnaire;
  private final boolean formLoaded;
  private final Optional<Form> form;
  
  @Override
  public Questionnaire getQuestionnaire() {
    return questionnaire;
  }

  @Override
  public Optional<String> getLookupPrefix() {
    if (!formLoaded) {
      return Optional.empty();
    }
    return Optional.of(FormInstanceQueryImpl.LOOKUP);
  }

  @Override
  public boolean isFormLoaded() {
    return formLoaded;
  }

  @Override
  public FormItem getFormItem(Answer answer) {
    if (!formLoaded) {
      return null;
    }
    
    try {
      final var valueset = questionnaire.getValueSets().stream().filter(p -> p.getId().equals(FormInstanceQueryImpl.LOOKUP + answer.getId())).findFirst().get();
      final var formItem = valueset.getEntries().iterator().next().getValue();
      
      return new JsonObject(formItem).mapTo(FormItem.class);
    } catch(Exception e) {
      // ignore failure on purpose
      log.error("Failed to resolve form: {} item for answer: {}, error: {}", 
          form.get().getMetadata().getLabel(), 
          answer.getId(),
          e.getMessage(), 
          e);
      return null;
    }
  }

  public record AnswerAndValueSet(
      Answer answer,
      FormItem item,
      Optional<FormValueSet> valueSet,
      Optional<FormValueSetEntry> valueSetEntry) {}

  @Override
  public Optional<String> encodeFormPrettily() {
    if(!this.formLoaded) {
      return Optional.empty();
    }
    
    final var encodeFormPrettily = new DialobFormPrettyPrinter().printForm(JsonObject.mapFrom(this.form.get()));
    return Optional.of(encodeFormPrettily);
  }

  @Override
  public Answer answer(String name) {
    return findEntry(questionnaire.getAnswers(), name);
  }
  
  @Override
  public String text(String name) {
    Answer answer = answer(name);
    if(answer == null || answer.getValue() == null) {
      return "";
    }
    return answer.getValue().toString();
  }

  @Override
  public BigDecimal decimal(String name) {
    Answer answer = findEntry(questionnaire.getAnswers(), name);
    if (answer == null || answer.getValue() == null) {
      return BigDecimal.ZERO; 
    }
    assert answer.getType().equals("INTEGER") || answer.getType().equals("DECIMAL");
    return new BigDecimal(answer.getValue().toString());
  }
  
  @Override
  public boolean bool(String name) {
    Answer answer = findEntry(questionnaire.getAnswers(), name);
    if (answer == null || answer.getValue() == null) {
      return false;
    }
    assert answer.getType().equals("BOOLEAN");
    return Boolean.TRUE.equals(answer.getValue());
  }
  
  @Override
  public Object variable(String name) {
    VariableValue var = findVariable(questionnaire.getVariableValues(), name);
    return var.getValue();
  }
  
  @Override
  public Object context(String name) {
    ContextValue result = findContext(questionnaire.getContext(), name);
    return result == null ? null : result.getValue();
  }
  
  private Answer findEntry(List<Answer> list, String entryKey) {
    return list.stream().filter(it -> it.getId().equals(entryKey)).findFirst().orElse(null);
  }
  
  private VariableValue findVariable(List<VariableValue> variableValues, String key) {
    return variableValues.stream().filter(it -> it.getId().equals(key)).findFirst().orElse(null);
  }
  
  private ContextValue findContext(List<ContextValue> contextValues, String key) {
    return contextValues.stream().filter(it -> it.getId().equals(key)).findFirst().orElse(null);
  }
}