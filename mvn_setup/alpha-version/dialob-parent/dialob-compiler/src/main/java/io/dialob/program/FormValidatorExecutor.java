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
package io.dialob.program;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormValidationError;
import io.dialob.spi.FormValidator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FormValidatorExecutor {

  private final List<FormValidator> formValidators;

  public FormValidatorExecutor(List<FormValidator> formValidators) {
    this.formValidators = formValidators;
  }

  @Nonnull
  public List<FormValidationError> validate(@Nonnull Form form) {
    return formValidators.stream()
      .map(formValidator -> formValidator.validate(form))
      .flatMap(List::stream)
      .collect(Collectors.toList());
  }

}
