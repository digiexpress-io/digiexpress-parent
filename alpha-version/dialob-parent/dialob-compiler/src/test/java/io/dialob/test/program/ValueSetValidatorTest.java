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

import io.dialob.api.form.Form;
import io.dialob.api.form.FormValidationError;
import io.dialob.api.form.ImmutableForm;
import io.dialob.api.form.ImmutableFormMetadata;
import io.dialob.api.form.ImmutableFormValueSet;
import io.dialob.api.form.ImmutableFormValueSetEntry;
import io.dialob.program.ValueSetValidator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class ValueSetValidatorTest {
  private final ValueSetValidator valueSetValidator = new ValueSetValidator();
  private final Form baseForm = ImmutableForm.builder()
      .name("test")
      .metadata(ImmutableFormMetadata.builder()
        .addLanguages("en")
        .label("TestForm")
        .build()
      ).build();

  @Test
  public void shouldWarnOnEmptySet() {
    ImmutableForm testForm = ImmutableForm.builder()
      .from(baseForm)
      .addValueSets(
        ImmutableFormValueSet.builder()
          .id("emptySetId")
          .build()
      ).build();

    List<FormValidationError> errors = valueSetValidator.validate(testForm);

    assertThat(errors).extracting("type", "level", "message", "itemId").containsOnly(
      tuple(FormValidationError.Type.VALUESET, FormValidationError.Level.WARNING, "VALUESET_EMPTY", "emptySetId")
    );

  }

  @Test
  public void shouldErrorOnDuplicateKeys() {
    ImmutableForm testForm = ImmutableForm.builder()
      .from(baseForm)
      .addValueSets(
        ImmutableFormValueSet.builder()
          .id("setId")
          .addEntries(
            ImmutableFormValueSetEntry.builder()
              .id("a")
              .putLabel("en", "One")
              .build(),
            ImmutableFormValueSetEntry.builder()
              .id("a")
              .putLabel("en", "Two")
              .build(),
            ImmutableFormValueSetEntry.builder()
              .id("b")
              .putLabel("en", "Three")
              .build()
          ).build()
      ).build();

    List<FormValidationError> errors = valueSetValidator.validate(testForm);

    assertThat(errors).extracting("type", "level", "message", "itemId", "expression", "index").containsOnly(
      tuple(FormValidationError.Type.VALUESET, FormValidationError.Level.ERROR, "VALUESET_DUPLICATE_KEY", "setId", Optional.of("a"), Optional.of(0)),
      tuple(FormValidationError.Type.VALUESET, FormValidationError.Level.ERROR, "VALUESET_DUPLICATE_KEY", "setId", Optional.of("a"), Optional.of(1))
    );

  }

  @Test
  public void shouldPassValidSet() {
    ImmutableForm testForm = ImmutableForm.builder()
      .from(baseForm)
      .addValueSets(
        ImmutableFormValueSet.builder()
          .id("setId")
          .addEntries(
            ImmutableFormValueSetEntry.builder()
              .id("a")
              .putLabel("en", "One")
              .build(),
            ImmutableFormValueSetEntry.builder()
              .id("b")
              .putLabel("en", "Two")
              .build(),
            ImmutableFormValueSetEntry.builder()
              .id("c")
              .putLabel("en", "Three")
              .build()
          ).build()
      ).build();

    List<FormValidationError> errors = valueSetValidator.validate(testForm);

    Assertions.assertEquals(0, errors.size());
  }

  @Test
  public void shouldErrorOnEmptyKey() {
    ImmutableForm testForm = ImmutableForm.builder()
      .from(baseForm)
      .addValueSets(
        ImmutableFormValueSet.builder()
          .id("setId")
          .addEntries(
            ImmutableFormValueSetEntry.builder()
              .id("a")
              .putLabel("en", "One")
              .build(),
            ImmutableFormValueSetEntry.builder()
              .id("")
              .putLabel("en", "Two")
              .build(),
            ImmutableFormValueSetEntry.builder()
              .id("c")
              .putLabel("en", "Three")
              .build()
          ).build()
      ).build();

    List<FormValidationError> errors = valueSetValidator.validate(testForm);

    assertThat(errors).extracting("type", "level", "message", "itemId", "index").containsOnly(
      tuple(FormValidationError.Type.VALUESET, FormValidationError.Level.ERROR, "VALUESET_EMPTY_KEY", "setId", Optional.of(1))
    );
  }
}
