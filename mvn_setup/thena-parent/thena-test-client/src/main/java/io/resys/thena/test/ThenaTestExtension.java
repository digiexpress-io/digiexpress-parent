package io.resys.thena.test;

import java.util.Optional;

/*-
 * #%L
 * thena-test-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.sqlclient.Pool;

public class ThenaTestExtension implements BeforeAllCallback, AfterAllCallback, ParameterResolver {
  private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ThenaTestExtension.class);
  
  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    final Class<?> type = parameterContext.getParameter().getType();
    return type == Pool.class || 
           type == Vertx.class || 
           type == ThenaTestContext.class;
  }


  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    final var ctx = getContext(extensionContext);
    if(ctx.isEmpty()) {
      return null;
    }
    
    final var type = parameterContext.getParameter().getType();
    
    if (type == Pool.class) return ctx.get().getPool();
    if (type == Vertx.class) return ctx.get().getVertx();
    if (type == ThenaTestContext.class) return ctx.get();
    
    return null;
  }
  
  
  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    
    final Class<?> testClass = context.getRequiredTestClass();
    Optional<ThenaTest> thenaTest = AnnotationSupport
        .findAnnotation(testClass, ThenaTest.class);
    
    if(thenaTest.isEmpty()) {
      thenaTest = AnnotationSupport
        .findAnnotation(Class.forName(testClass.getAnnotatedSuperclass().getType().getTypeName()), ThenaTest.class);
    }
    
    if(thenaTest.isEmpty()) {
      return;
    }
    
    // Create your context/resources
    final var thenaContext = new ThenaTestContext();
    thenaContext.initialize(thenaTest.get());
    
    // Store it in the ExtensionContext
    context.getStore(NAMESPACE).put(ThenaTestContext.class.getCanonicalName(), new CloseableThenaContext(thenaContext));
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    
  }
  
  private Optional<ThenaTestContext> getContext(ExtensionContext context) {
    final var closable = context.getStore(NAMESPACE)
        .get(ThenaTestContext.class.getCanonicalName(), CloseableThenaContext.class);
    
    if(closable == null) {
      return Optional.empty();
    }
    
    return Optional.ofNullable(closable.get());
  }
  
  
  private static class CloseableThenaContext implements ExtensionContext.Store.CloseableResource {
    private final ThenaTestContext context;
    
    CloseableThenaContext(ThenaTestContext context) {
      this.context = context;
    }
    
    public ThenaTestContext get() {
      return context;
    }
    
    @Override
    public void close() {
      context.cleanup();
    }
  }
}
