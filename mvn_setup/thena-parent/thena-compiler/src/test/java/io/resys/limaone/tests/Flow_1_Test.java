package io.resys.limaone.tests;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.limaone.tests.support.TestTemplate;
import io.vertx.core.json.JsonObject;



public class Flow_1_Test {

  @Test
  public void firstMatch() throws IOException {
    
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
""",            
    TestTemplate.Deps.dt("decision/match-first.json"),
    TestTemplate.Deps.dt("decision/match-last.json")); 
    
    
    {            
      Map<String, Serializable> values = new HashMap<>();
      values.put("clientType", "company");
      values.put("test", true);
      values.put("passed", false);
      values.put("age", 50);
      values.put("lang", "es");

      
      final var result = envir.run(values).andGetBody();
      Assertions.assertEquals("{}", JsonObject.mapFrom(result.getReturns()).encode());
    }
  }

}
