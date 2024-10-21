package io.digiexpress.eveli.client.spi;

import java.math.BigDecimal;
import java.util.List;

import io.dialob.api.questionnaire.Answer;
import io.dialob.api.questionnaire.ContextValue;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.api.questionnaire.VariableValue;
import io.digiexpress.eveli.client.api.DialobCommands.Dialob;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DialobImpl implements Dialob {
  private final Questionnaire questionnaire;
  
  public Answer answer(String name) {
    return findEntry(questionnaire.getAnswers(), name);
  }
  
  public String text(String name) {
    Answer answer = answer(name);
    if(answer == null || answer.getValue() == null) {
      return "";
    }
    return answer.getValue().toString();
  }

  public BigDecimal decimal(String name) {
    Answer answer = findEntry(questionnaire.getAnswers(), name);
    if (answer == null || answer.getValue() == null) {
      return BigDecimal.ZERO; 
    }
    assert answer.getType().equals("INTEGER") || answer.getType().equals("DECIMAL");
    return new BigDecimal(answer.getValue().toString());
  }
  
  public boolean bool(String name) {
    Answer answer = findEntry(questionnaire.getAnswers(), name);
    if (answer == null || answer.getValue() == null) {
      return false;
    }
    assert answer.getType().equals("BOOLEAN");
    return Boolean.TRUE.equals(answer.getValue());
  }
  
  public Object variable(String name) {
    VariableValue var = findVariable(questionnaire.getVariableValues(), name);
    return var.getValue();
  }
  
  public Object context(String name) {
    ContextValue result = findContext(questionnaire.getContext(), name);
    return result == null ? null : result.getValue();
  }

  public Questionnaire.Metadata metadata() {
    return questionnaire.getMetadata();
  }
  
  public Answer findEntry(List<Answer> list, String entryKey) {
    return list.stream().filter(it -> it.getId().equals(entryKey)).findFirst().orElse(null);
  }
  
  public VariableValue findVariable(List<VariableValue> variableValues, String key) {
    return variableValues.stream().filter(it -> it.getId().equals(key)).findFirst().orElse(null);
  }
  
  public ContextValue findContext(List<ContextValue> contextValues, String key) {
    return contextValues.stream().filter(it -> it.getId().equals(key)).findFirst().orElse(null);
  }

  @Override
  public Questionnaire unwrap() {
    return questionnaire;
  }
}