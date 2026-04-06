package io.resys.limaone.tests;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.limaone.tests.support.TestTemplate;



public class Flow_1_Test {

  @Test
  public void firstMatch() {
    
    final var envir = TestTemplate.compileOneFlow("""
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
        language: lang
          
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
""",            
    TestTemplate.Deps.dt("decision/match-first.json"),
    TestTemplate.Deps.dt("decision/match-last.json")); 
    
    
    TestTemplate.compileOneDt("decision/match-first.json").encodePrettily();
    
    {            
      Map<String, Serializable> values = new HashMap<>();
      values.put("clientType", "company");
      values.put("test", true);
      values.put("passed", false);
      values.put("age", 50);
      values.put("lang", "es");

      final var result = envir.run(values).andEncodePrettily();
      Assertions.assertEquals(
"""
FLOW NAME: optional-dt-combination
HISTORY: splitFlow -> splitFlow -> last-match -> format_text

┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ STEP {1} splitFlow : COMPLETED                                                                                       │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ ACCEPTS                                          │ RETURNS                                                           │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ clientType: company                              │ clientType == "private": false                                    │
└──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ STEP {2} splitFlow[2] : COMPLETED                                                                                    │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ ACCEPTS                                          │ RETURNS                                                           │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ clientType: company                              │ clientType == "company": true                                     │
└──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ STEP {3} last-match : COMPLETED                                                                                      │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ ACCEPTS                                          │ RETURNS                                                           │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ lan: es                                          │                                                                   │
│ age: 50                                          │                                                                   │
└──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ STEP {4} format_text : COMPLETED                                                                                     │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ ACCEPTS                                          │ RETURNS                                                           │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
└──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
""", result);
      
    }
  }

}
