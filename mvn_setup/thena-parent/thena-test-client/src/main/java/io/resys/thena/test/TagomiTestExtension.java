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
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

import io.resys.thena.test.TagomiTest.TagomiUrl;

public class TagomiTestExtension implements BeforeAllCallback, AfterAllCallback, ParameterResolver {
  private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(TagomiTestExtension.class);

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    final Class<?> type = parameterContext.getParameter().getType();
    return type == TagomiUrl.class ||
           type == TagomiTestContext.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    final var ctx = getContext(extensionContext);
    if (ctx.isEmpty()) {
      return null;
    }

    final var type = parameterContext.getParameter().getType();
    if (type == TagomiUrl.class) return ctx.get().getTagomiUrl();
    if (type == TagomiTestContext.class) return ctx.get();
    return null;
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    final Class<?> testClass = context.getRequiredTestClass();
    Optional<TagomiTest> tagomiTest = AnnotationSupport.findAnnotation(testClass, TagomiTest.class);

    if (tagomiTest.isEmpty()) {
      tagomiTest = AnnotationSupport
          .findAnnotation(Class.forName(testClass.getAnnotatedSuperclass().getType().getTypeName()), TagomiTest.class);
    }

    if (tagomiTest.isEmpty()) {
      return;
    }

    final var tagomiContext = new TagomiTestContext();
    tagomiContext.initialize(tagomiTest.get());

    context.getStore(NAMESPACE).put(TagomiTestContext.class.getCanonicalName(), new CloseableTagomiContext(tagomiContext));
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
  }

  private Optional<TagomiTestContext> getContext(ExtensionContext context) {
    var ctx = context;
    while (ctx != null) {
      final var closable = ctx.getStore(NAMESPACE)
          .get(TagomiTestContext.class.getCanonicalName(), CloseableTagomiContext.class);
      if (closable != null) {
        return Optional.ofNullable(closable.get());
      }
      ctx = ctx.getParent().orElse(null);
    }
    return Optional.empty();
  }

  private static class CloseableTagomiContext implements ExtensionContext.Store.CloseableResource {
    private final TagomiTestContext context;

    CloseableTagomiContext(TagomiTestContext context) {
      this.context = context;
    }

    public TagomiTestContext get() {
      return context;
    }

    @Override
    public void close() {
      context.cleanup();
    }
  }
}
