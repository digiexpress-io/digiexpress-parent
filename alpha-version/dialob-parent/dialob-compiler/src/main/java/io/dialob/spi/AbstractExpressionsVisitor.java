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
package io.dialob.spi;

import java.util.Optional;

import javax.annotation.Nonnull;

import io.dialob.api.form.FormItem;


public abstract class AbstractExpressionsVisitor implements FormVisitor {

  protected abstract void expression(String id, String classifier, String expression);

  protected abstract void condition(String id, String classifier, String condition);

  @Override
  public Optional<FormItemVisitor> startFormItems() {
    return Optional.of(new FormItemVisitor() {

      private FormItem formItem;

      @Override
      public void end() {
        if (formItem == null) {
          throw new IllegalStateException("Do not calls visitor end yet.");
        }
        condition(formItem.getId(), "activeWhen", formItem.getActiveWhen());
        condition(formItem.getId(), "required", formItem.getRequired());
        formItem = null;
      }

      @Override
      public Optional<ValidationVisitor> startValidations() {
        return Optional.of(validation -> condition(formItem.getId(), "validation:" + validation.getMessage(), validation.getRule()));
      }

      @Override
      public void visitQuestionnaireItem(@Nonnull FormItem formItem) {
        this.formItem = formItem;
      }

      @Override
      public void visitGroup(@Nonnull FormItem formItem) {
        this.formItem = formItem;
      }

      @Override
      public void visitSurveyGroup(@Nonnull FormItem formItem) {
        this.formItem = formItem;
      }

      @Override
      public void visitRowGroup(@Nonnull FormItem formItem) {
        this.formItem = formItem;
      }

      @Override
      public void visitPage(@Nonnull FormItem formItem) {
        this.formItem = formItem;
      }

      @Override
      public void visitQuestion(@Nonnull FormItem formItem) {
        this.formItem = formItem;
      }

      @Override
      public void visitNote(@Nonnull FormItem formItem) {
        this.formItem = formItem;
      }
    });
  }

  @Override
  public Optional<FormVariableVisitor> startFormVariables() {
    return Optional.of(formVariable -> expression(formVariable.getName(), null, formVariable.getExpression()));
  }

}
