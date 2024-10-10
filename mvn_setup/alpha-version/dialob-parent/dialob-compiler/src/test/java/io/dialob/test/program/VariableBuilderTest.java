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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.dialob.api.form.FormValidationError;
import io.dialob.api.form.ImmutableFormValidationError;
import io.dialob.program.ProgramBuilder;
import io.dialob.program.VariableBuilder;

class VariableBuilderTest {

  @Test
  public void shouldValidateContextVariable() {
    Consumer<FormValidationError> errorConsumer = Mockito.mock(Consumer.class);
    ProgramBuilder programBuilder = Mockito.mock(ProgramBuilder.class);
    String id = "id";

    new VariableBuilder(programBuilder, id)
      .setContext(true)
      .setType("number")
      .setDefaultValue("err")
      .afterExpressionCompilation(errorConsumer);

    verify(errorConsumer).accept(ImmutableFormValidationError.builder()
      .itemId("id")
      .type(FormValidationError.Type.GENERAL)
      .level(FormValidationError.Level.ERROR)
      .message("INVALID_DEFAULT_VALUE")
      .build());



    verifyNoMoreInteractions(errorConsumer);
  }


  @Test
  public void shouldValidateContextVariableWithMissingType() {
    Consumer<FormValidationError> errorConsumer = Mockito.mock(Consumer.class);
    ProgramBuilder programBuilder = Mockito.mock(ProgramBuilder.class);
    String id = "id";

    new VariableBuilder(programBuilder, id)
      .setContext(true)
      .setDefaultValue("err")
      .afterExpressionCompilation(errorConsumer);

    verify(errorConsumer).accept(ImmutableFormValidationError.builder()
      .itemId("id")
      .type(FormValidationError.Type.VARIABLE)
      .level(FormValidationError.Level.ERROR)
      .message("CONTEXT_VARIABLE_UNDEFINED_TYPE")
      .build());

    verifyNoMoreInteractions(errorConsumer);
  }


}
