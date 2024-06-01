package io.dialob.client.api;

import org.immutables.value.Value;

import io.dialob.client.spi.event.QuestionnaireEventPublisher;
import io.dialob.client.spi.function.AsyncFunctionInvoker;
import io.dialob.compiler.DialobProgramFromFormCompiler;
import io.dialob.program.DialobSessionEvalContextFactory;

@Value.Immutable
public interface DialobClientConfig {
  DialobCache getCache();
  DialobSessionEvalContextFactory getFactory();
  AsyncFunctionInvoker getAsyncFunctionInvoker();
  QuestionnaireEventPublisher getEventPublisher();
  DialobProgramFromFormCompiler getCompiler();
  DialobErrorHandler getErrorHandler();
}