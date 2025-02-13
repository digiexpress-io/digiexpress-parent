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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormValidationError;
import io.dialob.api.form.ImmutableFormValidationError;
import io.dialob.program.FormValidatorExecutor;
import io.dialob.spi.FormValidator;

public class FormValidatorExecutorTest {

  @Test
  public void shouldCombineErrors() {
    FormValidator valA = mock(FormValidator.class);
    when(valA.validate(any(Form.class))).thenReturn(Arrays.asList(
      ImmutableFormValidationError.builder().message("a").build(),
      ImmutableFormValidationError.builder().message("b").build()
    ));

    FormValidator valB = mock(FormValidator.class);
    when(valB.validate(any(Form.class))).thenReturn(Arrays.asList(
      ImmutableFormValidationError.builder().message("c").build(),
      ImmutableFormValidationError.builder().message("d").build()
    ));

    List<FormValidator> validators = Arrays.asList(valA, valB);

    FormValidatorExecutor formValidatorExecutor = new FormValidatorExecutor(validators);
    List<FormValidationError> results = formValidatorExecutor.validate(mock(Form.class));

    assertThat(results).extracting( "message").containsOnly("a","b", "c", "d");
  }

}
