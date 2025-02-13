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

import io.dialob.api.form.Form;
import io.dialob.api.form.FormItem;
import io.dialob.api.form.ImmutableFormItem;
import io.dialob.api.form.Validation;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.*;

@Slf4j
public class VisitableForm {

  private final Form form;

  private VisitableForm(@Nonnull Form form) {
    this.form = form;
  }

  public static VisitableForm makeVisitableForm(@Nonnull Form form) {
    return new VisitableForm(form);
  }

  public void accept(@Nonnull final FormVisitor visitor) {
    visitor.start();
    visitor.visitForm(form);
    visitor.visitFormMetadata(form.getMetadata());

    // All pages
    final Set<String> pages = findAllPages(form.getData());

    visitor.startFormItems().ifPresent(formItemVisitor -> {
      final HashSet<String> keys = new HashSet<>(form.getData().keySet());

      final Queue<String> order = new ArrayDeque<>();
      order.add(Constants.QUESTIONNAIRE);

      while (!order.isEmpty()) {
        final String key = order.remove();
        keys.remove(key);
        FormItem formItem = form.getData().get(key);
        if (formItem == null) {
          log.warn("Could not find item: {}", key);
          continue;
        }
        if (pages.contains(formItem.getId())) {
          formItem = ImmutableFormItem.builder().from(formItem)
            .type("page").build();
        }
        formItemVisitor.start();
        final String type = formItem.getType();
        if (type != null) {
          switch (type) {
            case "questionnaire":
              formItemVisitor.visitQuestionnaireItem(formItem);
              order.addAll(formItem.getItems());
              break;
            case "group":
              formItemVisitor.visitGroup(formItem);
              order.addAll(formItem.getItems());
              break;
            case "rowgroup":
              formItemVisitor.visitRowGroup(formItem);
              order.addAll(formItem.getItems());
              break;
            case "surveygroup":
              formItemVisitor.visitSurveyGroup(formItem);
              order.addAll(formItem.getItems());
              break;
            case "page":
              formItemVisitor.visitPage(formItem);
              order.addAll(formItem.getItems());
              break;
            case "note":
              formItemVisitor.visitNote(formItem);
              break;
            default:
              formItemVisitor.visitQuestion(formItem);
              break;
          }
          final List<Validation> validations = formItem.getValidations();
          formItemVisitor.startValidations().ifPresent(validationVisitor -> {
            validationVisitor.start();
            validations.forEach(validationVisitor::visitValidation);
            validationVisitor.end();
          });
          formItemVisitor.endValidations();
        }
        formItemVisitor.end();
      }
      if (!keys.isEmpty()) {
        log.warn("Items without hoisting group: {}", keys);
      }
    });
    visitor.endFormItems();

    visitor.startFormVariables().ifPresent(formVariableVisitor -> {
      formVariableVisitor.start();
      form.getVariables().forEach(formVariableVisitor::visitFormVariable);
      formVariableVisitor.end();
    });
    visitor.endFormVariables();

    visitor.startValueSets().ifPresent(valueSetVisitor -> {
      valueSetVisitor.start();
      form.getValueSets().forEach(valueSetVisitor::visitValueSet);
      valueSetVisitor.end();
    });
    visitor.endValueSets();

    visitor.end();
  }

  public Set<String> findAllPages(@Nonnull Map<String, FormItem> items) {
    final Set<String> pages = new HashSet<>();
    for (FormItem formItem : items.values()) {
      if ("questionnaire".equals(formItem.getType())) {
        pages.addAll(formItem.getItems());
        break;
      }
    }
    return pages;
  }

}
