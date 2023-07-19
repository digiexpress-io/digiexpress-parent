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
package io.dialob.program.expr.arith;

import io.dialob.program.EvalContext;
import io.dialob.rule.parser.PeriodUtil;
import io.dialob.rule.parser.api.ValueType;

import org.immutables.value.Value;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.LocalTime;

@Value.Immutable
public interface TimePlusDurationOperator extends InfixOperator<LocalTime> {
  @Override
  default Object eval(@Nonnull EvalContext evalContext) {
    LocalTime localTime = (LocalTime) getLhs().eval(evalContext);
    Duration duration = (Duration) getRhs().eval(evalContext);
    if (duration == null || localTime == null) {
      return null;
    }
    return PeriodUtil.timePlusDuration(localTime, duration);
  }

  @Nonnull
  @Override
  default ValueType getValueType() {
    return ValueType.TIME;
  }
}
