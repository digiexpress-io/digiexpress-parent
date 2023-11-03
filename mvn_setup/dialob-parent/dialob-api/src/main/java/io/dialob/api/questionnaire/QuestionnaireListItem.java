package io.dialob.api.questionnaire;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
@JsonSerialize(as = ImmutableQuestionnaireListItem.class)
@JsonDeserialize(as = ImmutableQuestionnaireListItem.class)
@Gson.TypeAdapters
public interface QuestionnaireListItem extends Serializable {

  String getId();

  Questionnaire.Metadata getMetadata();

}
