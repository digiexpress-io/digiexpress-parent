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
import io.dialob.api.form.Form;
import io.dialob.compiler.DialobProgramErrorsException;
import io.dialob.compiler.DialobProgramFromFormCompiler;
import io.dialob.program.DialobProgram;
import io.dialob.program.DialobRuleExpressionCompiler;
import io.dialob.rule.parser.api.RuleExpressionCompiler;
import io.dialob.rule.parser.function.FunctionRegistry;
import io.dialob.rule.parser.service.AbstractRuleExpressionCompilerTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class DialobRuleExpressionCompilerTest extends AbstractRuleExpressionCompilerTest {

  final ObjectMapper mapper = new ObjectMapper().registerModules(new JavaTimeModule());

  private Form parseForm(String resource) throws IOException {
    return mapper.readValue(this.getClass().getResourceAsStream(resource), Form.class);
  }

  @Test
  public void testDialobRuleSuccessfulBuild() throws Exception {
    Form form = parseForm("/form_ffrl.json");

    FunctionRegistry functionRegistry = Mockito.mock(FunctionRegistry.class);

    DialobProgramFromFormCompiler dialobProgramFromFormCompiler = new DialobProgramFromFormCompiler(functionRegistry);

    DialobProgram result = dialobProgramFromFormCompiler.compileForm(form);
    assertNotNull(result);
    verifyNoMoreInteractions(functionRegistry);
  }

  @Test
  public void testDialobRuleFailedBuild() throws Exception {
    Form form = parseForm("/form_ffrl_errors.json");

    FunctionRegistry functionRegistry = Mockito.mock(FunctionRegistry.class);

    DialobProgramFromFormCompiler dialobProgramFromFormCompiler = new DialobProgramFromFormCompiler(functionRegistry);

    DialobProgramErrorsException e = Assertions.assertThrows(DialobProgramErrorsException.class, () -> dialobProgramFromFormCompiler.compileForm(form));
    assertThat(e.getErrors()).extracting("message").containsExactlyInAnyOrder(
      "SYNTAX_ERROR",
      "UNKNOWN_VARIABLE",
      "COULD_NOT_DEDUCE_TYPE",
      "SYNTAX_ERROR",
      "SYNTAX_ERROR",
      "INVALID_DEFAULT_VALUE"
    );
    verifyNoMoreInteractions(functionRegistry);
  }

  @Override
  protected RuleExpressionCompiler createRuleExpressionCompiler() {
    return new DialobRuleExpressionCompiler();
  }
}
