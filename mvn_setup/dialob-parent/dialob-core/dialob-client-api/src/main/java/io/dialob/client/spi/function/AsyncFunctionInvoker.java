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
package io.dialob.client.spi.function;

import java.util.Collections;

import javax.annotation.Nonnull;

import io.dialob.api.proto.ActionsFactory;
import io.dialob.client.api.QuestionnaireSession;
import io.dialob.executor.model.IdUtils;
import io.dialob.program.EvalContext;
import io.dialob.rule.parser.function.FunctionRegistry;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AsyncFunctionInvoker {

  private final FunctionRegistry registry;

  public EvalContext.UpdatedItemsVisitor.AsyncFunctionCallVisitor createVisitor(QuestionnaireSession session) {
    return asyncFunctionCall -> {
      final var callback = new AsyncFunctionCallback(session, IdUtils.toString(asyncFunctionCall.getTargetId().get()));
      final var functionName = asyncFunctionCall.getFunctionName();
      final var args = asyncFunctionCall.getArgs();
      
      registry.invokeFunctionAsync(callback, functionName, args);
    };
  }

  @RequiredArgsConstructor
  private static class AsyncFunctionCallback implements FunctionRegistry.FunctionCallback {
    private final QuestionnaireSession session;
    private final String targetId;

    @Override
    public void succeeded(Object result) {
      final var action = ActionsFactory.setValue(targetId, result);
      session.dispatchActions(Collections.singletonList(action));
    }

    @Override
    public void failed(@Nonnull String error) {
      final var action = ActionsFactory.setValue(targetId, error);
      session.dispatchActions(Collections.singletonList(action));
    }
  }
}
