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
package io.dialob.compiler;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormItem;
import io.dialob.api.form.FormValidationError;
import io.dialob.api.form.FormValueSetEntry;
import io.dialob.api.form.Validation;
import io.dialob.program.DialobProgram;
import io.dialob.program.ProgramBuilder;
import io.dialob.program.QuestionBuilder;
import io.dialob.program.ValueSetBuilder;
import io.dialob.program.model.Program;
import io.dialob.rule.parser.function.FunctionRegistry;
import io.dialob.spi.FormItemVisitor;
import io.dialob.spi.FormValueSetVisitor;
import io.dialob.spi.FormVariableVisitor;
import io.dialob.spi.FormVisitor;
import io.dialob.spi.VisitableForm;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DialobProgramFromFormCompiler {

  private final FunctionRegistry functionRegistry;

  public DialobProgramFromFormCompiler(FunctionRegistry functionRegistry) {
    this.functionRegistry = functionRegistry;
  }


  /**
   * Converts FormDocument to DialobProgram
   *
   * @param formDocument
   * @throws DialobProgramErrorsException if any compilation errors found
   * @return compiled DialobProgram
   */
  public DialobProgram compileForm(@Nonnull Form formDocument) {
    ProgramBuilder builder = new ProgramBuilder(functionRegistry);
    VisitableForm.makeVisitableForm(formDocument).accept(new FormVisitor() {

      private boolean answersRequiredByDefault = false;

      Set<String> languages;

      private String formLabel;

      @Override
      public void start() {
        builder.startProgram();
      }

      @Override
      public void visitForm(@Nonnull Form formDocument) {
        builder.setId(formDocument.getId());
        formLabel = formDocument.getMetadata().getLabel();
        languages = formDocument.getMetadata().getLanguages();
        Object allRequiredByDefault = formDocument.getMetadata().getAdditionalProperties().get("answersRequiredByDefault");
        if (Boolean.TRUE.equals(allRequiredByDefault) ||
          allRequiredByDefault instanceof String && BooleanUtils.toBoolean((String) allRequiredByDefault) ) {
          this.answersRequiredByDefault = true;
        }
        if (languages.isEmpty()) {
          languages = Sets.newHashSet("en");
        }
      }

      @Override
      public Optional<FormItemVisitor> startFormItems() {
        return Optional.of(new FormItemVisitor() {


          @Override
          public void visitQuestionnaireItem(@Nonnull FormItem formItem) {
            builder
              .addRoot()
              .setView(formItem.getView())
              .setLabel(Maps.asMap(languages, input -> formLabel))
              .addItems(formItem.getItems())
              .addClassnames(formItem.getClassName())
              .setProps(formItem.getProps())
              .build();
          }

          @Override
          public void visitGroup(@Nonnull FormItem formItem) {
            builder.addGroup(formItem.getId())
              .setView(formItem.getView())
              .setLabel(formItem.getLabel())
              .setDescription(formItem.getDescription())
              .setActiveWhen(formItem.getActiveWhen())
              .addItems(formItem.getItems())
              .addClassnames(formItem.getClassName())
              .setProps(formItem.getProps())
              .build();
          }

          @Override
          public void visitSurveyGroup(@Nonnull FormItem formItem) {
            builder.addSurveyGroup(formItem.getId())
              .setView(formItem.getView())
              .setLabel(formItem.getLabel())
              .setDescription(formItem.getDescription())
              .setActiveWhen(formItem.getActiveWhen())
              .addItems(formItem.getItems())
              .addClassnames(formItem.getClassName())
              .setValueSet(formItem.getValueSetId())
              .setProps(formItem.getProps())
              .build();
          }

          @Override
          public void visitRowGroup(@Nonnull FormItem formItem) {
            builder.addRowGroup(formItem.getId())
              .setView(formItem.getView())
              .setLabel(formItem.getLabel())
              .setDescription(formItem.getDescription())
              .setActiveWhen(formItem.getActiveWhen())
              .setCanAddRowWhen(formItem.getCanAddRowWhen())
              .setCanRemoveRowWhen(formItem.getCanRemoveRowWhen())
              .addItems(formItem.getItems())
              .addClassnames(formItem.getClassName())
              .setValueSet(formItem.getValueSetId())
              .setProps(formItem.getProps())
              .build();
          }

          @Override
          public void visitPage(@Nonnull FormItem formItem) {
            builder.addPage(formItem.getId())
              .setView(formItem.getView())
              .setLabel(formItem.getLabel())
              .setDescription(formItem.getDescription())
              .setActiveWhen(formItem.getActiveWhen())
              .addItems(formItem.getItems())
              .addClassnames(formItem.getClassName())
              .setProps(formItem.getProps())
              .build();
          }

          @Override
          public void visitQuestion(@Nonnull FormItem formItem) {
            QuestionBuilder questionBuilder = builder.addQuestion(formItem.getId())
              .setLabel(formItem.getLabel())
              .setDescription(formItem.getDescription())
              .setActiveWhen(formItem.getActiveWhen())
              .setType(formItem.getType())
              .setView(formItem.getView())
              .setDefaultValue(formItem.getDefaultValue())
              .setValueSet(formItem.getValueSetId())
              .setProps(formItem.getProps())
              .addClassnames(formItem.getClassName());

            if (StringUtils.isNotBlank(formItem.getRequired())) {
              questionBuilder.setRequiredWhen(formItem.getRequired());
            } else if (answersRequiredByDefault && !"note".equals(formItem.getType())) {
              questionBuilder.setRequired(true);
            }

            int i = 1;
            // TODO not good idea to use hardcoded filtering here
            if (!"note".equals(formItem.getType())) {
              for (Validation validationBean : formItem.getValidations()) {
                questionBuilder.addValidation(formItem.getId() + "_error" + (i++))
                  .setActiveWhen(validationBean.getRule())
                  .setLabel(validationBean.getMessage())
                  .build();
              }
            }
            questionBuilder.build();
          }

          @Override
          public void visitNote(@Nonnull FormItem formItem) {
            this.visitQuestion(formItem);
          }
        });
      }

      @Nonnull
      @Override
      public Optional<FormValueSetVisitor> startValueSets() {
        return Optional.of(valueSet -> {
          ValueSetBuilder valueSetBuilder = builder.addValueSet(valueSet.getId());
          for (FormValueSetEntry entry : valueSet.getEntries()) {
            valueSetBuilder.addValue(entry.getId())
              .setActiveWhen(entry.getWhen())
              .setLabel(entry.getLabel())
              .build();
          }
          valueSetBuilder.build();
        });
      }

      @Nonnull
      @Override
      public Optional<FormVariableVisitor> startFormVariables() {
        return Optional.of(formVariable -> builder.addVariable(formVariable.getName())
          .setContext(formVariable.getContext())
          .setPublished(formVariable.getPublished())
          .setValueExpression(formVariable.getExpression())
          .setDefaultValue(formVariable.getDefaultValue())
          .setType(formVariable.getContextType())
          .build());
      }
    });

    final Program program = builder.build();
    if (program != null && LOGGER.isDebugEnabled()) {
      LOGGER.debug(program.toString());
    }
    final List<FormValidationError> builderErrors = builder.getErrors();
    if (!builderErrors.isEmpty() || program == null) {
      throw new DialobProgramErrorsException("Could not compile program due errors.", builderErrors);
    }
    return DialobProgram.createDialobProgram(program);
  }
}
