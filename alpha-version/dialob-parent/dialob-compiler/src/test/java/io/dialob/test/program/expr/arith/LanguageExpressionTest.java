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

import io.dialob.executor.command.event.SessionLocaleUpdatedEvent;
import io.dialob.program.EvalContext;
import io.dialob.program.expr.arith.LanguageExpression;
import io.dialob.program.expr.arith.StringOperators;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class LanguageExpressionTest {

  @Test
  public void shouldGetCurrentLanguageFromContext() {
    EvalContext context = Mockito.mock(EvalContext.class);

    doReturn("fi").when(context).getLanguage();
    assertEquals("fi", StringOperators.languageOperator().eval(context));
    verify(context).getLanguage();
    verifyNoMoreInteractions(context);
  }

  @Test
  public void shouldDependOnLocaleChanges() {
    LanguageExpression languageExpression = StringOperators.languageOperator();
    assertTrue(languageExpression.getEvalRequiredConditions().iterator().next().matches(SessionLocaleUpdatedEvent.INSTANCE));
  }
}
