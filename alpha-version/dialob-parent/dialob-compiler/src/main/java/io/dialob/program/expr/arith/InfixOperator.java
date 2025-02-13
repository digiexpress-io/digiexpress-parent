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

import java.util.Set;

import javax.annotation.Nonnull;

import org.immutables.value.Value;

import com.google.common.collect.ImmutableSet;

import io.dialob.executor.command.EventMatcher;
import io.dialob.program.model.Expression;

/**
 * Represents operator between two expressions.

 * @param <T> expressions evaluates value type
 */
public interface InfixOperator<T> extends Expression {

  /**
   * @return left hand side expression of operator
   */
  @Value.Parameter
  Expression getLhs();

  /**
   * @return right hand side expression of operator
   */
  @Value.Parameter
  Expression getRhs();

  @Nonnull
  @Override
  default Set<EventMatcher> getEvalRequiredConditions() {
    Set<EventMatcher> lset = getLhs().getEvalRequiredConditions();
    Set<EventMatcher> rset = getRhs().getEvalRequiredConditions();
    if (lset.isEmpty()) {
      return rset;
    }
    if (rset.isEmpty()) {
      return lset;
    }
    return ImmutableSet.<EventMatcher>builder().addAll(lset).addAll(rset).build();
  }

}
