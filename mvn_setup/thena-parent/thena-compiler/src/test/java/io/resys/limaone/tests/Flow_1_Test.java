package io.resys.limaone.tests;

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
