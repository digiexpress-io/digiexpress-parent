/*
 * Copyright 2018 ReSys OÜ
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.api.questionnaire;


public final class QuestionnaireFactory {

  private QuestionnaireFactory() {}

  public static Answer answer(String id, Object value) {
    return ImmutableAnswer.builder().id(id).value(value).build();
  }

  public static ContextValue contextValue(String id, Object value) {
    return ImmutableContextValue.builder().id(id).value(value).build();
  }

  public static VariableValue variableValue(String id, Object value) {
    return ImmutableVariableValue.builder().id(id).value(value).build();
  }

  public static Questionnaire questionnaire(String id, String formId) {
    return ImmutableQuestionnaire.builder().id(id)
          .metadata(ImmutableQuestionnaireMetadata.builder().formId(formId).build()).build();
  }

}
