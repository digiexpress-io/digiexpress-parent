/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.test.program;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.dialob.api.form.*;
import io.dialob.compiler.DialobProgramFromFormCompiler;
import io.dialob.program.DialobFormValidator;
import io.dialob.rule.parser.api.ValueType;
import io.dialob.rule.parser.api.VariableNotDefinedException;
import io.dialob.rule.parser.function.FunctionRegistry;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class DialobFormValidatorTest {

  ObjectMapper objectMapper = new ObjectMapper().registerModules(new JavaTimeModule());

  private Form loadForm(String formFile) {
    try {
      return objectMapper.readValue(Thread.currentThread().getContextClassLoader().getResourceAsStream(formFile), Form.class);
    } catch (Exception e) {
      Assertions.fail("Could not load " + formFile,e);
    }
    return null;
  }

  @Test
  public void shouldReportMissingExpressionOnVariable() {
    FunctionRegistry functionRegistry = Mockito.mock(FunctionRegistry.class);
    DialobProgramFromFormCompiler compiler = new DialobProgramFromFormCompiler(functionRegistry);
    DialobFormValidator validator = new DialobFormValidator(compiler);

    List<FormValidationError> errors = validator.validate(ImmutableForm.builder()
      .id("123")
      .metadata(ImmutableFormMetadata.builder().label("").build())
      .putData("questionnaire", ImmutableFormItem.builder().id("questionnaire").type("questionnaire").build())
      .addVariables(ImmutableVariable.builder().name("var1").build())
      .build());

    Assertions.assertThat(errors).contains(ImmutableFormValidationError.builder().itemId("var1").type(FormValidationError.Type.VARIABLE).message("RB_VARIABLE_NEEDS_EXPRESSION").build());

    Mockito.verifyNoMoreInteractions(functionRegistry);
  }

  @Test
  public void shouldReportIncompatibleComparison() throws IOException {
    FunctionRegistry functionRegistry = Mockito.mock(FunctionRegistry.class);
    DialobProgramFromFormCompiler compiler = new DialobProgramFromFormCompiler(functionRegistry);
    DialobFormValidator validator = new DialobFormValidator(compiler);

    Form form = loadForm("io/dialob/session/engine/issue-171.json");

    List<FormValidationError> errors = validator.validate(form);

    Assertions.assertThat(errors).contains(ImmutableFormValidationError.builder()
      .itemId("text1")
      .type(FormValidationError.Type.VISIBILITY)
      .message("NO_ORDER_RELATION_BETWEEN_TYPES")
      .startIndex(0)
      .endIndex(18)
      .build());

    Mockito.verifyNoMoreInteractions(functionRegistry);
  }

  @Test
  public void issue233() throws IOException {
    FunctionRegistry functionRegistry = Mockito.mock(FunctionRegistry.class);
    DialobProgramFromFormCompiler compiler = new DialobProgramFromFormCompiler(functionRegistry);
    DialobFormValidator validator = new DialobFormValidator(compiler);

    Form form = loadForm("io/dialob/session/engine/issue-233.json");

    List<FormValidationError> errors = validator.validate(form);

    Assertions.assertThat(errors).contains(ImmutableFormValidationError.builder()
      .itemId("text1")
      .type(FormValidationError.Type.VALIDATION)
      .message("MATCHER_REGEX_SYNTAX_ERROR")
      .startIndex(0)
      .endIndex(38)
      .build());

    Mockito.verifyNoMoreInteractions(functionRegistry);
  }

  @Test
  public void issue233b() throws IOException {
    FunctionRegistry functionRegistry = Mockito.mock(FunctionRegistry.class);
    DialobProgramFromFormCompiler compiler = new DialobProgramFromFormCompiler(functionRegistry);
    DialobFormValidator validator = new DialobFormValidator(compiler);

    Form form = loadForm("io/dialob/session/engine/issue-233b.json");

    List<FormValidationError> errors = validator.validate(form);

    Assertions.assertThat(errors).contains(ImmutableFormValidationError.builder()
      .itemId("text1")
      .type(FormValidationError.Type.VALIDATION)
      .message("MATCHER_DYNAMIC_REGEX")
      .startIndex(0)
      .endIndex(25)
      .build());

    Mockito.verifyNoMoreInteractions(functionRegistry);
  }


  @Test
  public void issue275() throws IOException, VariableNotDefinedException {
    FunctionRegistry functionRegistry = Mockito.mock(FunctionRegistry.class);
    when(functionRegistry.isAsyncFunction("count")).thenReturn(false);

    when(functionRegistry.returnTypeOf("count", new ValueType[]{ValueType.arrayOf(ValueType.STRING)})).thenReturn(ValueType.INTEGER);


    DialobProgramFromFormCompiler compiler = new DialobProgramFromFormCompiler(functionRegistry);
    DialobFormValidator validator = new DialobFormValidator(compiler);

    Form form = loadForm("io/dialob/session/engine/issue-275.json");

    List<FormValidationError> errors = validator.validate(form);

    Assertions.assertThat(errors.isEmpty()).isTrue();

    Mockito.verify(functionRegistry, times(2)).isAsyncFunction("count");
    Mockito.verify(functionRegistry).returnTypeOf("count", ValueType.arrayOf(ValueType.STRING));
    Mockito.verifyNoMoreInteractions(functionRegistry);
  }

  @Test
  public void validateOfExpressions() throws IOException {
    FunctionRegistry functionRegistry = Mockito.mock(FunctionRegistry.class);
    DialobProgramFromFormCompiler compiler = new DialobProgramFromFormCompiler(functionRegistry);
    DialobFormValidator validator = new DialobFormValidator(compiler);

    Form form = ImmutableForm.builder()
      .id("yyy")
      .name("zzz")
      .metadata(ImmutableFormMetadata.builder()
        .label("xxx")
        .build())
      .putData("questionnaire", ImmutableFormItem.builder().id("questionnaire").type("questionnaire").addItems("page").build())
      .putData("page", ImmutableFormItem.builder().id("page").type("group").addItems("rg","o1","o2","o3","o4").build())
      .putData("rg", ImmutableFormItem.builder().id("rg").type("rowgroup").addItems("q1","q2","q3","q4","q5").build())
      .putData("q1", ImmutableFormItem.builder().id("q1").type("number").build())
      .putData("q2", ImmutableFormItem.builder().id("q2").type("decimal").build())
      .putData("q3", ImmutableFormItem.builder().id("q3").type("boolean").build())
      .putData("q4", ImmutableFormItem.builder().id("q4").type("text").build())
      .putData("q5", ImmutableFormItem.builder().id("q5").type("note").activeWhen("sum of q1 > 0").build())
      .putData("o1", ImmutableFormItem.builder().id("o1").type("note").activeWhen("sum of q1 > 0").build())
      .putData("o2", ImmutableFormItem.builder().id("o2").type("note").activeWhen("xxx of q3").build())
      .putData("o3", ImmutableFormItem.builder().id("o3").type("note").activeWhen("sum of q4 = \"\"").build())
      .putData("o4", ImmutableFormItem.builder().id("o4").type("note").activeWhen("sum of (1)").build())

      .build();

    Assertions.assertThat(validator.validate(form)).extracting("itemId", "message").containsExactlyInAnyOrder(
      tuple("o2", "UNKNOWN_REDUCER_OPERATOR"),
      tuple("o3", "OPERATOR_CANNOT_REDUCE_TYPE"),
      tuple("rg.*.q5", "CANNOT_USE_REDUCER_INSIDE_SCOPE"),
      tuple("rg.*.q5", "COULD_NOT_DEDUCE_TYPE"),
      tuple("o4", "REDUCER_TARGET_MUST_BE_REFERENCE")
    );


    Mockito.verifyNoMoreInteractions(functionRegistry);
  }


}
