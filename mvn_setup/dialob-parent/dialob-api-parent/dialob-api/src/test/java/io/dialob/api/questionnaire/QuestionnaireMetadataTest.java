/*
 * Copyright 2017 ReSys OÜ
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.api.questionnaire;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestionnaireMetadataTest {
  @Test
  public void shouldDeserializeUnknownAttributesToAdditionalProperties() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    Questionnaire.Metadata metadata = objectMapper.readValue("{\"formId\":\"123\",\"status\":\"NEW\",\"extraProp\":\"extraValue\"}", Questionnaire.Metadata.class);
    assertTrue(metadata.getAdditionalProperties().size() > 0);
    assertEquals("extraValue", metadata.getAdditionalProperties().get("extraProp"));
  }
  @Test
  public void shouldSerializeAdditionalPropertiesToJsonAttributes() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    Questionnaire.Metadata metadata = ImmutableQuestionnaireMetadata.builder().formId("123").putAdditionalProperties("extraProp","extraValue").build();
    assertEquals("{\"formId\":\"123\",\"status\":\"NEW\",\"extraProp\":\"extraValue\"}", objectMapper.writeValueAsString(metadata));
  }
}
