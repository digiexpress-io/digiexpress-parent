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
package io.dialob.executor.command;

import io.dialob.executor.model.ErrorState;
import io.dialob.program.EvalContext;

import org.immutables.value.Value;

import javax.annotation.Nonnull;

@Value.Immutable
public interface UpdateValidationCommand extends ErrorUpdateCommand {

  @Nonnull
  @Override
  default ErrorState update(@Nonnull EvalContext context, @Nonnull ErrorState state) {
    final Object eval = getExpression().eval(context);
    ErrorState.UpdateBuilder updateBuilder = state.update(context).setActive(eval != null ? (Boolean) eval : false);
    return updateBuilder.get();
  }

}
