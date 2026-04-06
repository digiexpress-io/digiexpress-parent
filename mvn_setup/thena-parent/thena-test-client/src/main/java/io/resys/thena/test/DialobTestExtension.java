package io.resys.thena.test;

/*-
 * #%L
 * thena-test-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.Optional;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

import io.resys.thena.test.DialobTest.DialobResetDB;
import io.resys.thena.test.DialobTest.FormUrl;

public class DialobTestExtension implements BeforeAllCallback, AfterAllCallback, ParameterResolver, BeforeEachCallback {
  private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(DialobTestExtension.class);
  
  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    final Class<?> type = parameterContext.getParameter().getType();
    return type == FormUrl.class ||  
           type == DialobTestContext.class;
  }


  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    final var ctx = getContext(extensionContext);
    if(ctx.isEmpty()) {
      return null;
    }
    
    final var type = parameterContext.getParameter().getType();
    if (type == FormUrl.class) return ctx.get().getFormUrl();
    if (type == DialobTestContext.class) return ctx.get();
    return null;
  }
  
  
  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    
    final Class<?> testClass = context.getRequiredTestClass();
    Optional<DialobTest> DialobTest = AnnotationSupport.findAnnotation(testClass, DialobTest.class);
    
    if(DialobTest.isEmpty()) {
      DialobTest = AnnotationSupport
        .findAnnotation(Class.forName(testClass.getAnnotatedSuperclass().getType().getTypeName()), DialobTest.class);
    }
    
    if(DialobTest.isEmpty()) {
      return;
    }
    
    // Create your context/resources
    final var DialobContext = new DialobTestContext();
    DialobContext.initialize(DialobTest.get());
    
    // Store it in the ExtensionContext
    context.getStore(NAMESPACE).put(DialobTestContext.class.getCanonicalName(), new CloseableDialobContext(DialobContext));
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    
  }
  
  
  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    final var method = context.getRequiredTestMethod();
    final var annotation = AnnotationSupport.findAnnotation(method, DialobResetDB.class);

    if(annotation.isPresent()) {
      final var dialobContext = getContext(context);
      if (dialobContext.isPresent()) {
        final var ctx = dialobContext.get();

        if (annotation.get().enabled()) {
            ctx.clearTestData();
        }
      }
    }
  }
  
  private Optional<DialobTestContext> getContext(ExtensionContext context) {
    final var closable = context.getStore(NAMESPACE)
        .get(DialobTestContext.class.getCanonicalName(), CloseableDialobContext.class);
    
    if(closable == null) {
      return Optional.empty();
    }
    
    return Optional.ofNullable(closable.get());
  }
  
  
  private static class CloseableDialobContext implements ExtensionContext.Store.CloseableResource {
    private final DialobTestContext context;
    
    CloseableDialobContext(DialobTestContext context) {
      this.context = context;
    }
    
    public DialobTestContext get() {
      return context;
    }
    
    @Override
    public void close() {
      context.cleanup();
    }
  }
}
