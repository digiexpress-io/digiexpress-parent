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

import io.resys.limaone.program.FlowProgram.FlowExecutionStatus;
import io.resys.limaone.program.FlowProgram.FlowResult;
import io.resys.limaone.tests.support.TestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * Object input mapping tests for flows using a Decision Table (DT) or a Flow Task (FT)
 * Covering happy path, mismatched names, extra inputs, missing inputs, null input and wrong input format
 * Testing summary:
 * - DT: happy path works as expected; unknown, extra, or missing columns don't cause errors, DT falls back to the catch-all row when nothing matches, status is always COMPLETED
 * - FT: happy path works as expected; unknown, extra, or missing fields cause a binding error, result is null and status is ERROR
 */
@Slf4j
public class Flow_6_Test {

  /**
   * DT - happy path - object field names match the DT column names
   * Expected: COMPLETED, result = "1"
   */
  @Test
  public void objectMappingTest_Dt_Success() {
    final var envir = TestTemplate.compileOneFlow(flow_dt(), TestTemplate.Deps.dtx(fundEval_dt()));

    final var wrapper = envir.run(Map.of("fundAnswers", "{\"fundQuestionA\": \"selectionA\", \"fundQuestionB\": \"selectionA\"}"));
    FlowResult flow = wrapper.andGetBody();


    Assertions.assertEquals(
      """
      FLOW NAME: mapping
      HISTORY: fundEval

      ┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
      │ STEP {1} fundEval : COMPLETED                                                                                        │
      ├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
      │ ACCEPTS                                          │ RETURNS                                                           │
      ├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
      │ fundQuestionB: selectionA                        │ result: 1                                                         │
      │ fundQuestionA: selectionA                        │                                                                   │
      └──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
      """, wrapper.andEncodePrettily());

    Assertions.assertEquals(FlowExecutionStatus.COMPLETED, flow.getStatus());
    Assertions.assertEquals("fundEval", flow.getStepId());
    Assertions.assertEquals("fundEval", flow.getShortHistory());
    Assertions.assertEquals(1, flow.getLogs().size());
    // both input columns are accepted
    Assertions.assertEquals(2, flow.getLogs().stream().findFirst().get().getAccepts().size());
    // no errors
    Assertions.assertEquals(0, flow.getLogs().stream().findFirst().get().getErrors().size());
    // result correct
    Assertions.assertEquals("1", flow.getReturns().get("result"));

  }

  /**
   * DT - input mismatch - object field names don't match any DT column
   * The DT ignores unknown columns and falls through to the catch-all row
   * Expected: COMPLETED, result = "-1"
   */
  @Test
  public void objectMappingTest_Dt_InputMismatch() {
    final var envir = TestTemplate.compileOneFlow(flow_dt(), TestTemplate.Deps.dtx(fundEval_dt()));

    final var wrapper = envir.run(Map.of("fundAnswers", "{\"wrongColumnNameA\": \"selectionA\", \"wrongColumnNameB\": \"selectionA\"}"));
    FlowResult flow = wrapper.andGetBody();


    Assertions.assertEquals(
      """
      FLOW NAME: mapping
      HISTORY: fundEval

      ┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
      │ STEP {1} fundEval : COMPLETED                                                                                        │
      ├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
      │ ACCEPTS                                          │ RETURNS                                                           │
      ├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
      │ wrongColumnNameB: selectionA                     │ result: -1                                                        │
      │ wrongColumnNameA: selectionA                     │                                                                   │
      └──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
      """, wrapper.andEncodePrettily());

    Assertions.assertEquals(FlowExecutionStatus.COMPLETED, flow.getStatus());
    Assertions.assertEquals("fundEval", flow.getStepId());
    Assertions.assertEquals("fundEval", flow.getShortHistory());
    Assertions.assertEquals(1, flow.getLogs().size());
    // both input columns are accepted, but matching logic ignores them
    Assertions.assertEquals(2, flow.getLogs().stream().findFirst().get().getAccepts().size());
    // no errors
    Assertions.assertEquals(0, flow.getLogs().stream().findFirst().get().getErrors().size());
    // result is the fallback value
    Assertions.assertEquals("-1", flow.getReturns().get("result"));

  }

  /**
   * DT – extra unknown column. 
   * The DT ignores the extra column and executes normally
   * Expected: COMPLETED, result = "1"
   */
  @Test
  public void objectMappingTest_Dt_AdditionalInput() {
    final var envir = TestTemplate.compileOneFlow(flow_dt(), TestTemplate.Deps.dtx(fundEval_dt()));

    final var wrapper = envir.run(Map.of("fundAnswers", "{\"fundQuestionA\": \"selectionA\", \"fundQuestionB\": \"selectionA\", \"additionalColumn\": \"selectionA\"}"));
    FlowResult flow = wrapper.andGetBody();


    Assertions.assertEquals(
      """
      FLOW NAME: mapping
      HISTORY: fundEval

      ┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
      │ STEP {1} fundEval : COMPLETED                                                                                        │
      ├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
      │ ACCEPTS                                          │ RETURNS                                                           │
      ├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
      │ additionalColumn: selectionA                     │ result: 1                                                         │
      │ fundQuestionB: selectionA                        │                                                                   │
      │ fundQuestionA: selectionA                        │                                                                   │
      └──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
      """, wrapper.andEncodePrettily());

    Assertions.assertEquals(FlowExecutionStatus.COMPLETED, flow.getStatus());
    Assertions.assertEquals("fundEval", flow.getStepId());
    Assertions.assertEquals("fundEval", flow.getShortHistory());
    Assertions.assertEquals(1, flow.getLogs().size());
    // all 3 input columns are accepted, but matching logic ignores the extra one
    Assertions.assertEquals(3, flow.getLogs().stream().findFirst().get().getAccepts().size());
    // no errors
    Assertions.assertEquals(0, flow.getLogs().stream().findFirst().get().getErrors().size());
    // result correct
    Assertions.assertEquals("1", flow.getReturns().get("result"));

  }

  /**
   * DT – missing input - one of the object field names is missing
   * The DT ignores the missing column and falls through to the catch-all row
   * Expected: COMPLETED, result = "-1"
   */
  @Test
  public void objectMappingTest_Dt_MissingInput() {
    final var envir = TestTemplate.compileOneFlow(flow_dt(), TestTemplate.Deps.dtx(fundEval_dt()));

    final var wrapper = envir.run(Map.of("fundAnswers", "{\"fundQuestionA\": \"selectionA\"}"));
    FlowResult flow = wrapper.andGetBody();


    Assertions.assertEquals(
      """
      FLOW NAME: mapping
      HISTORY: fundEval

      ┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
      │ STEP {1} fundEval : COMPLETED                                                                                        │
      ├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
      │ ACCEPTS                                          │ RETURNS                                                           │
      ├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
      │ fundQuestionA: selectionA                        │ result: -1                                                        │
      └──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
      """, wrapper.andEncodePrettily());

    Assertions.assertEquals(FlowExecutionStatus.COMPLETED, flow.getStatus());
    Assertions.assertEquals("fundEval", flow.getStepId());
    Assertions.assertEquals("fundEval", flow.getShortHistory());
    Assertions.assertEquals(1, flow.getLogs().size());
    // input column is accepted, no error for missing column
    Assertions.assertEquals(1, flow.getLogs().stream().findFirst().get().getAccepts().size());
    // no errors
    Assertions.assertEquals(0, flow.getLogs().stream().findFirst().get().getErrors().size());
    // result falls back to -1
    Assertions.assertEquals("-1", flow.getReturns().get("result"));
  }

  /**
   * DT – null input
   * DT falls through to the catch-all row
   * Expected: COMPLETED, result = "-1"
   */
  @Test
  public void objectMappingTest_Dt_NullInput() {
    final var envir = TestTemplate.compileOneFlow(flow_dt(), TestTemplate.Deps.dtx(fundEval_dt()));

    Map<String, Serializable> input = new HashMap<>();
    input.put("fundAnswers", null);
    final var wrapper = envir.run(input);
    FlowResult flow = wrapper.andGetBody();


    Assertions.assertEquals(
      """
      FLOW NAME: mapping
      HISTORY: fundEval

      ┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
      │ STEP {1} fundEval : COMPLETED                                                                                        │
      ├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
      │ ACCEPTS                                          │ RETURNS                                                           │
      ├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
      │                                                  │ result: -1                                                        │
      └──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
      """, wrapper.andEncodePrettily());

    Assertions.assertEquals(FlowExecutionStatus.COMPLETED, flow.getStatus());
    Assertions.assertEquals("fundEval", flow.getStepId());
    Assertions.assertEquals("fundEval", flow.getShortHistory());
    Assertions.assertEquals(1, flow.getLogs().size());
    // no inputs
    Assertions.assertEquals(0, flow.getLogs().stream().findFirst().get().getAccepts().size());
    // no errors
    Assertions.assertEquals(0, flow.getLogs().stream().findFirst().get().getErrors().size());
    // result falls back to -1
    Assertions.assertEquals("-1", flow.getReturns().get("result"));
  }

  /**
   * DT – wrong input format - the input is not an object
   * The DT ignores the input and falls through to the catch-all row
   * Expected: COMPLETED, result = "-1"
   */
  @Test
  public void objectMappingTest_Dt_WrongInputFormat() {
    final var envir = TestTemplate.compileOneFlow(flow_dt(), TestTemplate.Deps.dtx(fundEval_dt()));

    final var wrapper = envir.run(Map.of("fundAnswers", "This is not an object and cannot be mapped to DT"));
    FlowResult flow = wrapper.andGetBody();


    Assertions.assertEquals(
      """
      FLOW NAME: mapping
      HISTORY: fundEval

      ┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
      │ STEP {1} fundEval : COMPLETED                                                                                        │
      ├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
      │ ACCEPTS                                          │ RETURNS                                                           │
      ├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
      │                                                  │ result: -1                                                        │
      └──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
      """, wrapper.andEncodePrettily());

    Assertions.assertEquals(FlowExecutionStatus.COMPLETED, flow.getStatus());
    Assertions.assertEquals("fundEval", flow.getStepId());
    Assertions.assertEquals("fundEval", flow.getShortHistory());
    Assertions.assertEquals(1, flow.getLogs().size());
    // no input columns are accepted
    Assertions.assertEquals(0, flow.getLogs().stream().findFirst().get().getAccepts().size());
    // no errors
    Assertions.assertEquals(0, flow.getLogs().stream().findFirst().get().getErrors().size());
    // result is fallback value -1
    Assertions.assertEquals("-1", flow.getReturns().get("result"));

  }

  /**
   * FT – happy path - object field names match the FT input names
   * Expected: COMPLETED, result = 1
   */
  @Test
  public void objectMappingTest_Ft_Success() {
    final var envir = TestTemplate.compileOneFlow(flow_ft(), TestTemplate.Deps.ft("flow-task/FundEvalService.txt"));

    final var wrapper = envir.run(Map.of("fundAnswers", "{\"fundQuestionA\": \"selectionA\", \"fundQuestionB\": \"selectionA\"}"));
    FlowResult flow = wrapper.andGetBody();


    Assertions.assertEquals(
      """
      FLOW NAME: mapping
      HISTORY: fundEval
      
      ┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
      │ STEP {1} fundEval : COMPLETED                                                                                        │
      ├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
      │ ACCEPTS                                          │ RETURNS                                                           │
      ├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
      │ fundQuestionB: selectionA                        │ result: 1                                                         │
      │ fundQuestionA: selectionA                        │                                                                   │
      └──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
      """, wrapper.andEncodePrettily());

    Assertions.assertEquals(FlowExecutionStatus.COMPLETED, flow.getStatus());
    Assertions.assertEquals("fundEval", flow.getStepId());
    Assertions.assertEquals("fundEval", flow.getShortHistory());
    Assertions.assertEquals(1, flow.getLogs().size());
    // both input fields are accepted
    Assertions.assertEquals(2, flow.getLogs().stream().findFirst().get().getAccepts().size());
    // no errors
    Assertions.assertEquals(0, flow.getLogs().stream().findFirst().get().getErrors().size());
    // result correct
    Assertions.assertEquals(1, flow.getReturns().get("result"));

  }


  /**
   * FT – input mismatch - object field names don't match any FT input names
   * Compared to DT, the FT binder fails on unrecognised fields
   * Expected: ERROR, result = null
   */
  @Test
  public void objectMappingTest_Ft_InputMismatch() {
    final var envir = TestTemplate.compileOneFlow(flow_ft(), TestTemplate.Deps.ft("flow-task/FundEvalService.txt"));

    final var wrapper = envir.run(Map.of("fundAnswers", "{\"wrongInputNameA\": \"selectionA\", \"wrongInputNameB\": \"selectionA\"}"));
    FlowResult flow = wrapper.andGetBody();

    Assertions.assertEquals(
      """
      Message: Unrecognized field "wrongInputNameB" (class io.resys.limaone.spi.compiler.groovy.pkg_.FundEvalService$Input), not marked as ignorable (3 known properties: "metaClass", "fundQuestionA", "fundQuestionB"])
       at [Source: UNKNOWN; byte offset: #UNKNOWN] (through reference chain: io.resys.limaone.spi.compiler.groovy.pkg_.FundEvalService$Input["wrongInputNameB"])
       props: {
        "wrongInputNameB" : "selectionA",
        "wrongInputNameA" : "selectionA"
      }
       statement: {
        "collection" : false,
        "mapping" : {
          "assignments" : { },
          "deconstructors" : [ "fundAnswers" ],
          "deconstructing" : true,
          "taskId" : "fundEval",
          "type" : "MAPPING"
        },
        "flowTaskName" : "FundEvalService",
        "taskId" : "fundEval",
        "type" : "BODY_FLOW_TASK"
      }""", flow.getLogs().stream().findFirst().get().getErrors().get(0).getMsg());

    Assertions.assertEquals(FlowExecutionStatus.ERROR, flow.getStatus());
    Assertions.assertEquals("fundEval", flow.getStepId());
    Assertions.assertEquals("fundEval", flow.getShortHistory());
    Assertions.assertEquals(1, flow.getLogs().size());
    // no input fields are accepted
    Assertions.assertEquals(0, flow.getLogs().stream().findFirst().get().getAccepts().size());
    // errors present
    Assertions.assertEquals(2, flow.getLogs().stream().findFirst().get().getErrors().size());
    // result null due to error
    Assertions.assertNull(flow.getReturns().get("result"));

  }

  /**
   * FT – additional input - one of the object fields is extra
   * The FT rejects the extra field, unlike the DT which simply ignores extra fields
   * Expected: ERROR, result = null
   */
  @Test
  public void objectMappingTest_Ft_AdditionalInput() {
    final var envir = TestTemplate.compileOneFlow(flow_ft(), TestTemplate.Deps.ft("flow-task/FundEvalService.txt"));

    final var wrapper = envir.run(Map.of("fundAnswers", "{\"fundQuestionA\": \"selectionA\", \"fundQuestionB\": \"selectionA\", \"additionalInput\": \"selectionA\"}"));
    FlowResult flow = wrapper.andGetBody();

    Assertions.assertEquals(
      """
      Message: Unrecognized field "additionalInput" (class io.resys.limaone.spi.compiler.groovy.pkg_.FundEvalService$Input), not marked as ignorable (3 known properties: "metaClass", "fundQuestionA", "fundQuestionB"])
       at [Source: UNKNOWN; byte offset: #UNKNOWN] (through reference chain: io.resys.limaone.spi.compiler.groovy.pkg_.FundEvalService$Input["additionalInput"])
       props: {
        "additionalInput" : "selectionA",
        "fundQuestionB" : "selectionA",
        "fundQuestionA" : "selectionA"
      }
       statement: {
        "collection" : false,
        "mapping" : {
          "assignments" : { },
          "deconstructors" : [ "fundAnswers" ],
          "deconstructing" : true,
          "taskId" : "fundEval",
          "type" : "MAPPING"
        },
        "flowTaskName" : "FundEvalService",
        "taskId" : "fundEval",
        "type" : "BODY_FLOW_TASK"
      }""", flow.getLogs().stream().findFirst().get().getErrors().get(0).getMsg());

    Assertions.assertEquals(FlowExecutionStatus.ERROR, flow.getStatus());
    Assertions.assertEquals("fundEval", flow.getStepId());
    Assertions.assertEquals("fundEval", flow.getShortHistory());
    Assertions.assertEquals(1, flow.getLogs().size());
    // no input fields are accepted, even though there are 2 correct, but fail because of 1 extra
    Assertions.assertEquals(0, flow.getLogs().stream().findFirst().get().getAccepts().size());
    // errors present
    Assertions.assertEquals(2, flow.getLogs().stream().findFirst().get().getErrors().size());
    // result null due to error
    Assertions.assertNull(flow.getReturns().get("result"));

  }

  /**
   * FT – missing input - one of the object fields is missing
   * Missing field causes a binding error and execution fails
   * Expected: ERROR, result = null
   */
  @Test
  public void objectMappingTest_Ft_MissingInput() {
    final var envir = TestTemplate.compileOneFlow(flow_ft(), TestTemplate.Deps.ft("flow-task/FundEvalService.txt"));

    final var wrapper = envir.run(Map.of("fundAnswers", "{\"fundQuestionA\": \"selectionA\"}"));
    FlowResult flow = wrapper.andGetBody();

    Assertions.assertEquals(
      """
      Message: -
       props: {
        "fundQuestionA" : "selectionA"
      }
       statement: {
        "collection" : false,
        "mapping" : {
          "assignments" : { },
          "deconstructors" : [ "fundAnswers" ],
          "deconstructing" : true,
          "taskId" : "fundEval",
          "type" : "MAPPING"
        },
        "flowTaskName" : "FundEvalService",
        "taskId" : "fundEval",
        "type" : "BODY_FLOW_TASK"
      }""", flow.getLogs().stream().findFirst().get().getErrors().get(0).getMsg());

    Assertions.assertEquals(FlowExecutionStatus.ERROR, flow.getStatus());
    Assertions.assertEquals("fundEval", flow.getStepId());
    Assertions.assertEquals("fundEval", flow.getShortHistory());
    Assertions.assertEquals(1, flow.getLogs().size());
    // no input fields are accepted
    Assertions.assertEquals(0, flow.getLogs().stream().findFirst().get().getAccepts().size());
    // errors present
    Assertions.assertEquals(2, flow.getLogs().stream().findFirst().get().getErrors().size());
    // result null due to error
    Assertions.assertNull(flow.getReturns().get("result"));
  }

  /**
   * FT – null input
   * FT causes an error on null input
   * Expected: ERROR, result = null
   */
  @Test
  public void objectMappingTest_Ft_NullInput() {
    final var envir = TestTemplate.compileOneFlow(flow_ft(), TestTemplate.Deps.ft("flow-task/FundEvalService.txt"));

    Map<String, Serializable> input = new HashMap<>();
    input.put("fundAnswers", null);
    final var wrapper = envir.run(input);
    FlowResult flow = wrapper.andGetBody();

    Assertions.assertEquals(
      """
      Message: -
       props: { }
       statement: {
        "collection" : false,
        "mapping" : {
          "assignments" : { },
          "deconstructors" : [ "fundAnswers" ],
          "deconstructing" : true,
          "taskId" : "fundEval",
          "type" : "MAPPING"
        },
        "flowTaskName" : "FundEvalService",
        "taskId" : "fundEval",
        "type" : "BODY_FLOW_TASK"
      }""", flow.getLogs().stream().findFirst().get().getErrors().get(0).getMsg());

    Assertions.assertEquals(FlowExecutionStatus.ERROR, flow.getStatus());
    Assertions.assertEquals("fundEval", flow.getStepId());
    Assertions.assertEquals("fundEval", flow.getShortHistory());
    Assertions.assertEquals(1, flow.getLogs().size());
    // no inputs
    Assertions.assertEquals(0, flow.getLogs().stream().findFirst().get().getAccepts().size());
    // errors present
    Assertions.assertEquals(2, flow.getLogs().stream().findFirst().get().getErrors().size());
    // result null due to error
    Assertions.assertNull(flow.getReturns().get("result"));
  }

  /**
   * FT – wrong input format - the input is not an object
   * Wrong input format causes a binding error and execution fails
   * Expected: ERROR, result = null
   */
  @Test
  public void objectMappingTest_Ft_WrongInputFormat() {
    final var envir = TestTemplate.compileOneFlow(flow_ft(), TestTemplate.Deps.ft("flow-task/FundEvalService.txt"));

    final var wrapper = envir.run(Map.of("fundAnswers", "This is not an object and cannot be mapped to flow task inputs"));
    FlowResult flow = wrapper.andGetBody();


    Assertions.assertEquals(
      """
      Message: -
       props: { }
       statement: {
        "collection" : false,
        "mapping" : {
          "assignments" : { },
          "deconstructors" : [ "fundAnswers" ],
          "deconstructing" : true,
          "taskId" : "fundEval",
          "type" : "MAPPING"
        },
        "flowTaskName" : "FundEvalService",
        "taskId" : "fundEval",
        "type" : "BODY_FLOW_TASK"
      }""", flow.getLogs().stream().findFirst().get().getErrors().get(0).getMsg());

    Assertions.assertEquals(FlowExecutionStatus.ERROR, flow.getStatus());
    Assertions.assertEquals("fundEval", flow.getStepId());
    Assertions.assertEquals("fundEval", flow.getShortHistory());
    Assertions.assertEquals(1, flow.getLogs().size());
    // no input fields are accepted
    Assertions.assertEquals(0, flow.getLogs().stream().findFirst().get().getAccepts().size());
    // errors present
    Assertions.assertEquals(2, flow.getLogs().stream().findFirst().get().getErrors().size());
    // result null due to error
    Assertions.assertNull(flow.getReturns().get("result"));

  }

  public String fundEval_dt() {
    return 
      """
      name: fundEval
      hitPolicy: FIRST
      table: |
        | fundQuestionA:STRING                          | fundQuestionB:STRING                          | -> | result:STRING |
        |-----------------------------------------------|-----------------------------------------------|----|---------------|
        | in ["selectionA"]                             | in ["selectionA"]                             |    | 1             |
        | in["selectionB","selectionC","selectionD"]    |                                               |    | 0             |
        |                                               | in["selectionB","selectionC","selectionD"]    |    | 0             |
        |                                               |                                               |    | -1            |
      """;
  }

  public String flow_dt() {
    return
      """
      id: mapping
  
      inputs:
        fundAnswers:
          required: false
          type: OBJECT
  
      tasks:
        - fundEval:
          id: fundEval
          then: end
          decisionTable:
            ref: fundEval
            collection: false
            inputs: fundAnswers
      """;
  }

  public String flow_ft() {
    return
      """
      id: mapping
  
      inputs:
        fundAnswers:
          required: false
          type: OBJECT
  
      tasks:
        - fundEval:
          id: fundEval
          then: end
          service:
            ref: FundEvalService
            collection: false
            inputs: fundAnswers
      """;
  }

}
