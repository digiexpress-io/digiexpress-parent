package io.resys.limaone.spi.dialob.model;

import java.util.Optional;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormItem;
import io.dialob.api.form.FormValueSet;
import io.dialob.api.form.FormValueSetEntry;
import io.dialob.api.questionnaire.Answer;
import io.dialob.api.questionnaire.Questionnaire;
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
}