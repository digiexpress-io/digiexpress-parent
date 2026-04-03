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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.limaone.program.DecisionProgram.DecisionResult;
import io.resys.limaone.program.DecisionProgram.DecisionRow;
import io.resys.limaone.program.Program.ProgramStatus;
import io.resys.limaone.tests.support.TestTemplate;



public class DecisionTest {

  @Test
  public void readerNodeOrderTest() throws IOException {
    final var decisionTable = TestTemplate.compileOneDt("decision/dt.json");
    
    List<DecisionRow> rows = decisionTable.getRows();
    Assertions.assertEquals(0, rows.get(0).getOrder());
    Assertions.assertEquals(1, rows.get(1).getOrder());
    Assertions.assertEquals(2, rows.get(2).getOrder());
    Assertions.assertEquals(3, rows.get(3).getOrder());
    Assertions.assertEquals(4, rows.get(4).getOrder());
    Assertions.assertEquals(5, rows.size());
    
    
    Assertions.assertEquals(
        """
Decision Table: testDecisionTable
Hit Policy: ALL

| risk IN               | sri IN | sriBoolean IN | sriDate IN                  | category OUT | reason OUT | reasonBoolean OUT | reasonDate OUT |
|-----------------------|--------|---------------|-----------------------------|--------------|------------|-------------------|----------------|
| in ["CAREFUL", "NOT"] | [1..2] | false         | equals 2017-07-03T00:00:00Z | GREEN        |            | false             | 2017-07-03     |
| in ["CAREFUL"]        | >= 3   | false         |                             | GREY         | HIGH_RISK  | false             |                |
| not in ["MODERATE"]   | [1..5] | false         | equals 2017-07-03T00:00:00Z | GREEN        |            | false             |                |
|                       | > 6    | false         | equals 2017-07-03T00:00:00Z | GREY         | HIGH_RISK  | false             |                |
| in ["AGGRESSIVE"]     |        | false         | equals 2017-07-03T00:00:00Z | GREEN        |            | false             |                |
        """, decisionTable.encodePrettily());
    
  }

  
//Match 1
//{
//   "id": "sriBoolean",
//   "value": "false"
//},
//{
//   "id": "risk",
//   "value": "in [\"CAREFUL\", \"NOT\"]"
//},
//{
//   "id": "sri",
//   "value": "[1..2]"
//},
//{
//   "id": "sriDate",
//   "value": "equals 2017-07-03T00:00:00"
//},
//
//Match 3
//{
//   "id": "sriBoolean",
//   "value": "false"
//},
//{
//   "id": "risk",
//   "value": "not in [\"MODERATE\"]"
//},
//{
//   "id": "sri",
//   "value": "[1..5]"
//},
//{
//   "id": "sriDate",
//   "value": "equals 2017-07-03T00:00:00"
//}
  @Test
  public void executionTest() throws IOException {
    final var envir = TestTemplate.compileOneDt("decision/dt.json");

    Map<String, Serializable> values = new HashMap<>();
    values.put("sriBoolean", false);
    values.put("risk", "CAREFUL");
    values.put("sri", 1);
    values.put("sriDate", TestTemplate.parseLocalDate("2017-07-03"));
    
    DecisionResult result = envir.run(values).andGetBody();

    Assertions.assertEquals(2, result.getMatches().size());
    Assertions.assertEquals(0, result.getMatches().get(0).getOrder());
    Assertions.assertEquals(2, result.getMatches().get(1).getOrder());
  }

  
  @Test
  public void qinMatchingTest() throws IOException {
    final var envir = TestTemplate.compileOneDt("decision/dt3.json");
    Assertions.assertEquals(
        """
Decision Table: testDecisionTableQInExpression
Hit Policy: FIRST

| path IN                                   | sri IN | sriBoolean IN | sriDate IN                  | category OUT | reason OUT | reasonBoolean OUT | reasonDate OUT |
|-------------------------------------------|--------|---------------|-----------------------------|--------------|------------|-------------------|----------------|
| qin ["task/#/name", "task/#/description"] | [1..2] | false         | equals 2017-07-03T00:00:00Z | GREEN        |            | false             | 2017-07-03     |
| in ["comment.*"]                          | >= 3   | false         |                             | GREY         | HIGH_RISK  | false             |                |
        """, envir.encodePrettily());
    

    {
      Map<String, Serializable> values = new HashMap<>();
      values.put("sriBoolean", false);
      values.put("path", "xyz");
      values.put("sri", 1);
      values.put("sriDate", TestTemplate.parseLocalDate("2017-07-03"));
      DecisionResult result = envir.run(values).andGetBody();
      Assertions.assertEquals(0, result.getMatches().size());
    }
    
    {
      Map<String, Serializable> values = new HashMap<>();
      values.put("sriBoolean", false);
      values.put("path", "task/smt/name");
      values.put("sri", 1);
      values.put("sriDate", TestTemplate.parseLocalDate("2017-07-03"));
      DecisionResult result = envir.run(values).andGetBody();
      Assertions.assertEquals(1, result.getMatches().size());
    }

  }
  
  @Test
  public void nullEqualsNull() throws IOException {
    final var envir = TestTemplate.compileOneDt("decision/nullEqualsNull.json");
    
    Map<String, Serializable> values = new HashMap<>();
    values.put("risk", null);
    
    DecisionResult result = envir.run(values).andGetBody();

    Assertions.assertEquals(1, result.getMatches().size());
    
    Assertions.assertEquals(
        """
Decision Table: nullEqualsNull
Hit Policy: FIRST

| risk IN | match OUT |
|---------|-----------|
|         | success   |
        """, envir.encodePrettily());
  }
  
  @Test
  public void firstHitPolicy() throws IOException {
    final var envir = TestTemplate.compileOneDt("decision/firstHitPolicy.json");

    Map<String, Serializable> values = new HashMap<>();
    values.put("regionName", "FIN");
    DecisionResult result = envir.run(values).andGetBody();
    Assertions.assertEquals(1, result.getMatches().size());
    Assertions.assertEquals(0, result.getMatches().get(0).getOrder());


    values = new HashMap<>();
    values.put("regionName", "X");
    result = envir.run(values).andGetBody();
    Assertions.assertEquals(1, result.getMatches().size());
    Assertions.assertEquals(1, result.getMatches().get(0).getOrder());
    
    Assertions.assertEquals(
        """
Decision Table: testRegion
Hit Policy: FIRST

| regionName IN   | regionFactor OUT |
|-----------------|------------------|
| in["FIN","EST"] | 1                |
|                 | 10               |
        """, envir.encodePrettily());
  }

  @Test
  public void all() throws IOException {
    final var envir = TestTemplate.compileOneDt("decision/allHitPolicy.json");

    Map<String, Serializable> values = new HashMap<>();
    values.put("firstName", "Mark");
    DecisionResult result = envir.run(values).andGetBody();
    Assertions.assertEquals(2, result.getMatches().size());
    Assertions.assertEquals(
        """
Decision Table: hitPolicyExample
Hit Policy: ALL

| firstName IN       | output OUT      |
|--------------------|-----------------|
| in["Mark","Peter"] | Found a match   |
|                    | Fall back value |
        """, envir.encodePrettily());
  }
  
  @Test
  public void valueSetTest() throws IOException {
    final var ast = TestTemplate.compileOneDt("decision/dtWithValueSet.json");
    List<String> valueSet = ast.getHeaders().get(0).getValueSet();
    Assertions.assertEquals(3, valueSet.size());
    
    Assertions.assertEquals(
        """
Decision Table: decimalTest
Hit Policy: FIRST

| letterCode IN | decimalValue OUT |
|---------------|------------------|
| ["M"]         | 0.5              |
| ["S"]         | 0.7              |
| ["L"]         | 1                |
        """, ast.encodePrettily());
  }

  @Test
  public void csvImportCommandRandoGarbageIn() throws IOException {
    final var ast = TestTemplate.compileOneDt("decision/dt-import.json");
    Assertions.assertEquals(ProgramStatus.UP, ast.getStatus());
    
    Assertions.assertEquals(
        """
Decision Table: cascoZipCodeDT
Hit Policy: FIRST

| zipCode IN  | otherFactor IN | colFactor IN | winFactor IN | parFactor IN | intFactor IN | Factor IN |
|-------------|----------------|--------------|--------------|--------------|--------------|-----------|
| in["00100"] | 1              | 1            | 1            | 1            | 1            | 1         |
| in["00120"] | 1              | 1            | 1            | 1            | 1            | 1         |
| in["00130"] | 1              | 1            | 1            | 1            | 1            | 1         |
| in["00140"] | 1              | 1            | 1            | 1            | 1            | 1         |
| in["00150"] | 1              | 1            | 1            | 1            | 1            | 1         |
        """, ast.encodePrettily());
  }


}
