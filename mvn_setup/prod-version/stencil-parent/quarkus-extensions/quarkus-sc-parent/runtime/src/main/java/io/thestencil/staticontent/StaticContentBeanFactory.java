package io.thestencil.staticontent;

/*-
 * #%L
 * quarkus-stencil-sc
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import java.util.Map;

@ApplicationScoped
public class StaticContentBeanFactory {
  
  private Map<String, String> serializedContent;
  private String defaultLocale;
  
  public StaticContentBeanFactory setDefaultLocale(String defaultLocale) {
    this.defaultLocale = defaultLocale;
    return this;
  }
  public StaticContentBeanFactory setSerializedContent(Map<String, String> serializedContent) {
    this.serializedContent = serializedContent;
    return this;
  }
  
  @Produces
  @Singleton
  @DefaultBean
  public ContentProvider inMemoryContentProvider() {
    return new InMemoryContentProvider(serializedContent, defaultLocale);
  }
}
