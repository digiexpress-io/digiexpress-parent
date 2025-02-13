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
package io.dialob.program;

import io.dialob.compiler.DialobSessionUpdateHook;
import io.dialob.executor.ActiveDialobSessionUpdater;
import io.dialob.executor.DialobSessionUpdater;
import io.dialob.executor.command.event.Event;
import io.dialob.executor.model.DialobSession;
import io.dialob.rule.parser.function.FunctionRegistry;

import javax.annotation.Nonnull;
import java.time.Clock;
import java.util.function.Consumer;

public class DialobSessionEvalContextFactory {

  private final FunctionRegistry functionRegistry;

  private final Clock clock;

  private final DialobSessionUpdateHook dialobSessionUpdateHook;

  public DialobSessionEvalContextFactory(FunctionRegistry functionRegistry, Clock clock, DialobSessionUpdateHook dialobSessionUpdateHook) {
    this.functionRegistry = functionRegistry;
    this.clock = clock;
    this.dialobSessionUpdateHook = dialobSessionUpdateHook;
  }

  @Nonnull
  public DialobSessionEvalContext createDialobSessionEvalContext(@Nonnull DialobSession dialobSession,
                                                                 @Nonnull Consumer<Event> updatesConsumer,
                                                                 boolean activating) {
    return new DialobSessionEvalContext(functionRegistry, dialobSession, updatesConsumer, clock, activating, dialobSessionUpdateHook);
  }


  public DialobSessionUpdater createSessionUpdater(@Nonnull DialobProgram dialobProgram, @Nonnull DialobSession dialobSession) {
    if (dialobSession.isCompleted()) {
      return DialobSessionUpdater.NOOP_UPDATER;
    }
    return new ActiveDialobSessionUpdater(this, dialobProgram, dialobSession);
  }

}
