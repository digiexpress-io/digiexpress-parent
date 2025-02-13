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
package io.dialob.test.program.expr.arith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ImmutableItemRef;
import io.dialob.executor.model.ImmutableValueSetId;
import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ValueSetState;
import io.dialob.program.EvalContext;
import io.dialob.program.ProgramBuilder;
import io.dialob.program.expr.OutputFormatter;
import io.dialob.program.expr.arith.LocalizedLabelOperator;
import io.dialob.program.model.Label;

class LocalizedLabelOperatorTest {

  @Mock
  ProgramBuilder programBuilder;
  @Mock
  EvalContext context;
  @Mock
  OutputFormatter outputFormatter;

  @BeforeEach
  public void beforeEach() {
    MockitoAnnotations.initMocks(this);
    when(programBuilder.findValueSetIdForItem(any(ItemId.class))).thenReturn(Optional.empty());
    when(outputFormatter.format(any())).then(invocation -> String.valueOf(invocation.<Object>getArgument(0)));
    when(context.getOutputFormatter()).thenReturn(outputFormatter);
  }

  @Test
  public void shouldReturnNullWhenLabelIsNotDefined() {
    LocalizedLabelOperator operator = LocalizedLabelOperator.createLocalizedLabelOperator(programBuilder, Label.createLabel(Collections.emptyMap()));
    when(context.getLanguage()).thenReturn("fi");
    assertNull(operator.eval(context));
    verify(context).getLanguage();
    verifyNoMoreInteractions(programBuilder, context);
  }

  @Test
  public void shouldReturnLocalizeLabel() {
    LocalizedLabelOperator operator = LocalizedLabelOperator.createLocalizedLabelOperator(programBuilder, Label.createLabel(ImmutableMap.of("fi","Otsikko","en","Title")));
    when(context.getLanguage()).thenReturn("fi").thenReturn("en");
    assertEquals("Otsikko", operator.eval(context));
    assertEquals("Title", operator.eval(context));
    verify(context, times(2)).getLanguage();
    verifyNoMoreInteractions(programBuilder, context);
  }

  @Test
  public void shouldExpandVariablesNonExistentExapndsToNull() {
    LocalizedLabelOperator operator = LocalizedLabelOperator.createLocalizedLabelOperator(programBuilder, Label.createLabel(ImmutableMap.of("fi","Otsikko {var1}")));
    when(context.getLanguage()).thenReturn("fi");
    assertEquals("Otsikko null", operator.eval(context));
    verify(context, times(1)).getLanguage();
    verify(context).getItemValue(ref("var1"));
    verify(programBuilder).findValueSetIdForItem(any(ItemId.class));
    verifyNoMoreInteractions(programBuilder, context);
  }

  private ItemId ref(String var1) {
    return (ImmutableItemRef) IdUtils.toId(var1);
  }


  @Test
  public void shouldExpandStringVariables() {
    LocalizedLabelOperator operator = LocalizedLabelOperator.createLocalizedLabelOperator(programBuilder, Label.createLabel(ImmutableMap.of("fi","Otsikko {var1}")));
    when(context.getLanguage()).thenReturn("fi");
    when(context.getItemValue(ref("var1"))).thenReturn("hello");
    assertEquals("Otsikko hello", operator.eval(context));
    verify(context, times(1)).getLanguage();
    verify(context).getItemValue(ref("var1"));
    verify(programBuilder).findValueSetIdForItem(any(ItemId.class));
    verify(context).getOutputFormatter();
    verifyNoMoreInteractions(programBuilder, context);
  }

  @Test
  public void shouldExpandNumberVariablesWithFormmater() {
    LocalizedLabelOperator operator = LocalizedLabelOperator.createLocalizedLabelOperator(programBuilder, Label.createLabel(ImmutableMap.of("fi","Otsikko {var1:#,##0}")));
    when(context.getLanguage()).thenReturn("fi");
    when(context.getItemValue(ref("var1"))).thenReturn(123000);
    assertEquals("Otsikko 123 000", operator.eval(context));
    verify(context, atLeastOnce()).getLanguage();
    verify(context).getItemValue(ref("var1"));
    verifyNoMoreInteractions(programBuilder, context);
  }
  @Test
  public void shouldExpandDecimalVariablesWithFormmater() {
    LocalizedLabelOperator operator = LocalizedLabelOperator.createLocalizedLabelOperator(programBuilder, Label.createLabel(ImmutableMap.of("fi","Otsikko {var1:#,##0.00}")));
    when(context.getLanguage()).thenReturn("fi");
    when(context.getItemValue(ref("var1"))).thenReturn(BigDecimal.valueOf(123000));
    assertEquals("Otsikko 123 000,00", operator.eval(context));
    verify(context, atLeastOnce()).getLanguage();
    verify(context).getItemValue(ref("var1"));
    verifyNoMoreInteractions(programBuilder, context);
  }
  @Test
  public void shouldExpandNumberlVariablesWithoutFormmater() {
    LocalizedLabelOperator operator = LocalizedLabelOperator.createLocalizedLabelOperator(programBuilder, Label.createLabel(ImmutableMap.of("fi","Otsikko {var1}")));
    when(context.getLanguage()).thenReturn("fi");
    when(context.getItemValue(ref("var1"))).thenReturn(123000);
    assertEquals("Otsikko 123000", operator.eval(context));
    verify(context, atLeastOnce()).getLanguage();
    verify(context).getItemValue(ref("var1"));
    verify(context).getOutputFormatter();
    verify(programBuilder).findValueSetIdForItem(any(ItemId.class));
    verifyNoMoreInteractions(programBuilder, context);
  }
  @Test
  public void shouldExpandDecimallVariablesWithoutFormmater() {
    LocalizedLabelOperator operator = LocalizedLabelOperator.createLocalizedLabelOperator(programBuilder, Label.createLabel(ImmutableMap.of("fi","Otsikko {var1}")));
    when(context.getLanguage()).thenReturn("fi");
    when(context.getItemValue(ref("var1"))).thenReturn(BigDecimal.valueOf(123000.01));
    assertEquals("Otsikko 123000.01", operator.eval(context));
    verify(context, atLeastOnce()).getLanguage();
    verify(context).getItemValue(ref("var1"));
    verify(context).getOutputFormatter();
    verify(programBuilder).findValueSetIdForItem(any(ItemId.class));
    verifyNoMoreInteractions(programBuilder, context);
  }
  @Test
  public void shouldInterpolateSelectionToValue() {
    when(programBuilder.findValueSetIdForItem(IdUtils.toId("var1"))).thenReturn(Optional.of("vs1"));

    LocalizedLabelOperator operator = LocalizedLabelOperator.createLocalizedLabelOperator(programBuilder, Label.createLabel(ImmutableMap.of("fi","Otsikko {var1}")));
    when(context.getLanguage()).thenReturn("fi");
    when(context.getItemValue(ref("var1"))).thenReturn("x1");
    ValueSetState valueSet = Mockito.mock(ValueSetState.class);
    when(valueSet.getEntries()).thenReturn(ImmutableList.of(ValueSetState.Entry.of("x1","Choice 1")));
    when(context.getValueSetState(ImmutableValueSetId.of("vs1"))).thenReturn(Optional.of(valueSet));
    assertEquals("Otsikko Choice 1", operator.eval(context));
    verify(context, atLeastOnce()).getLanguage();
    verify(context).getItemValue(ref("var1"));
    verify(context).getValueSetState(ImmutableValueSetId.of("vs1"));
    verify(programBuilder).findValueSetIdForItem(any(ItemId.class));
    verify(valueSet).getEntries();
    verifyNoMoreInteractions(programBuilder, context, valueSet);
  }
  @Test
  public void shouldInterpolateSelectionToLowerCaseValue() {
    when(programBuilder.findValueSetIdForItem(IdUtils.toId("var1"))).thenReturn(Optional.of("vs1"));

    LocalizedLabelOperator operator = LocalizedLabelOperator.createLocalizedLabelOperator(programBuilder, Label.createLabel(ImmutableMap.of("fi","Otsikko {var1:lowercase}")));
    when(context.getLanguage()).thenReturn("fi");
    when(context.getItemValue(ref("var1"))).thenReturn("x1");
    ValueSetState valueSet = Mockito.mock(ValueSetState.class);
    when(valueSet.getEntries()).thenReturn(ImmutableList.of(ValueSetState.Entry.of("x1","Choice 1")));
    when(context.getValueSetState(ImmutableValueSetId.of("vs1"))).thenReturn(Optional.of(valueSet));
    assertEquals("Otsikko choice 1", operator.eval(context));
    verify(context, atLeastOnce()).getLanguage();
    verify(context).getItemValue(ref("var1"));
    verify(context).getValueSetState(ImmutableValueSetId.of("vs1"));
    verify(programBuilder).findValueSetIdForItem(any(ItemId.class));
    verify(valueSet).getEntries();
    verifyNoMoreInteractions(programBuilder, context, valueSet);
  }

  @Test
  public void shouldInterpolateSelectionToUpperCaseValue() {
    when(programBuilder.findValueSetIdForItem(IdUtils.toId("var1"))).thenReturn(Optional.of("vs1"));

    LocalizedLabelOperator operator = LocalizedLabelOperator.createLocalizedLabelOperator(programBuilder, Label.createLabel(ImmutableMap.of("fi","Otsikko {var1:uppercase}")));
    when(context.getLanguage()).thenReturn("fi");
    when(context.getItemValue(ref("var1"))).thenReturn("x1");
    ValueSetState valueSet = Mockito.mock(ValueSetState.class);
    when(valueSet.getEntries()).thenReturn(ImmutableList.of(ValueSetState.Entry.of("x1","Choice 1")));
    when(context.getValueSetState(ImmutableValueSetId.of("vs1"))).thenReturn(Optional.of(valueSet));
    assertEquals("Otsikko CHOICE 1", operator.eval(context));
    verify(context, atLeastOnce()).getLanguage();
    verify(context).getItemValue(ref("var1"));
    verify(context).getValueSetState(ImmutableValueSetId.of("vs1"));
    verify(programBuilder).findValueSetIdForItem(any(ItemId.class));
    verify(valueSet).getEntries();
    verifyNoMoreInteractions(programBuilder, context, valueSet);
  }
  @Test
  public void shouldInterpolateSelectionToKeyWhenFormatIsKey() {
    LocalizedLabelOperator operator = LocalizedLabelOperator.createLocalizedLabelOperator(programBuilder, Label.createLabel(ImmutableMap.of("fi","Otsikko {var1:key}")));
    when(context.getLanguage()).thenReturn("fi");
    when(context.getItemValue(ref("var1"))).thenReturn("x1");
    assertEquals("Otsikko x1", operator.eval(context));
    verify(context, atLeastOnce()).getLanguage();
    verify(context).getItemValue(ref("var1"));
    verify(context).getOutputFormatter();
    verifyNoMoreInteractions(programBuilder, context);
  }
}
