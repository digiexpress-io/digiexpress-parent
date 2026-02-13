package io.resys.hdes.client.test;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;

/*-
 * #%L
 * wrench-component-assets-Dt
 * %%
 * Copyright (C) 2016 - 2017 Copyright 2016 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.resys.hdes.client.api.programs.DecisionProgram.DecisionResult;
import io.resys.hdes.client.spi.util.FileUtils;
import io.resys.hdes.client.test.config.TestUtils;
import io.vertx.core.json.JsonObject;

public class FirstDecisionTest {

  @Test
  public void firstMatch() throws IOException {
    
    final var envir = TestUtils.client.envir().tagName("simulate")
        .addCommand().id("dt-1").decision(FileUtils.toString(getClass(), "decision/match-first.json")).build()
        .addCommand().id("dt-2").decision(FileUtils.toString(getClass(), "decision/match-last.json")).build()
        .addCommand().id("optional-dt-combination").flow(flow(
"""
id: optional-dt-combination
inputs:
  clientType:
    required: true
    type: STRING
  test:
    required: true
    type: BOOLEAN
  passed:
    required: true
    type: BOOLEAN
  lang:
    required: true
    type: STRING
  age:
    required: true
    type: INTEGER
    
tasks:
  - split flow:
    id: splitFlow
    switch: 
      - private:
          when: clientType == \"private\"
          then: first-match
      - company:
          when: clientType == \"company\"
          then: last-match

  - first-match:
    id: first-match
    then: format_text
    decisionTable:
      ref: first-row-null-result
      collection: false
      inputs:
        test: test
        passed: passed
        language: lan
          
  - last-match:
    id: last-match
    then: format_text
    decisionTable:
      ref: last-row-null-result
      collection: false
      inputs:
        age: age
        lan: lang
          
  - format text:
    id: format_text
    then: end
    returns:
        inputs:
          ageLiabilityTxt: last-match.ageText
          knowlidgeLiabilityTxt: first-match.educationText
"""))
        .build()
        .build(); 
  
    
    
    {
  
      Map<String, Serializable> values = new HashMap<>();
      values.put("test", true);
      values.put("passed", false);
      values.put("language", "fi");
  
      DecisionResult result = TestUtils.client.executor(envir).inputMap(values).decision("first-row-null-result").andGetBody();
  
      Assertions.assertEquals(1, result.getMatches().size());
      Assertions.assertEquals(0, result.getMatches().get(0).getOrder());
      Assertions.assertEquals(0, result.getMatches().get(0).getReturns().size());
    }
    
    {

      Map<String, Serializable> values = new HashMap<>();
      values.put("age", 50);
      values.put("lan", "es");
  
      DecisionResult result = TestUtils.client.executor(envir).inputMap(values).decision("last-row-null-result").andGetBody();
  
      Assertions.assertEquals(1, result.getMatches().size());
      Assertions.assertEquals(4, result.getMatches().get(0).getOrder());
      Assertions.assertEquals(0, result.getMatches().get(0).getReturns().size());
    }
    
    
    {            
      Map<String, Serializable> values = new HashMap<>();
      values.put("clientType", "company");
      values.put("test", true);
      values.put("passed", false);
      values.put("age", 50);
      values.put("lang", "es");  

      
      final var result = TestUtils.client.executor(envir).inputMap(values).flow("optional-dt-combination")
          .andGetBody();
    
      Assertions.assertEquals("{}", JsonObject.mapFrom(result.getReturns()).encode());
    }
  }
  
  public String flow(String value) throws JsonProcessingException {
    return TestUtils.objectMapper.writeValueAsString(Arrays.asList(ImmutableAstCommand.builder()
      .type(AstCommandValue.SET_BODY)
      .value(value)
      .build()));
  }
}
