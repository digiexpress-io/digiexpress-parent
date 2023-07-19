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

import io.dialob.rule.parser.PeriodUtil;
import org.immutables.value.Value;

import java.time.Period;

@Value.Immutable
public interface PeriodGtOperator extends AbstractRelationOperator<Period> {

  @Override
  default boolean apply(int comp) {
    return comp > 0;
  }

  @Override
  default int compare(Period lhsResult, Period rhsResult) {
    return PeriodUtil.comparePeriods(lhsResult,  rhsResult);
  }

  @Override
  default String getOperator() {
    return ">";
  }
}
