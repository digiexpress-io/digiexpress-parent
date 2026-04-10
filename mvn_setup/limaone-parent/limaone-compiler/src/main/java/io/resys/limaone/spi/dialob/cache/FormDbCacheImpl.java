package io.resys.limaone.spi.dialob.cache;

/*-
 * #%L
 * limaone-compiler
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
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormTag;

public class FormDbCacheImpl implements FormDbCache {
  
  private final Cache<String, Form> formCache;
  private final Cache<String, FormTag> formTagCache;
  
  public FormDbCacheImpl() {
    this.formCache = Caffeine.newBuilder()
      .maximumSize(1000)
      .expireAfterWrite(30, TimeUnit.MINUTES)
      .build();
    
    this.formTagCache = Caffeine.newBuilder()
      .maximumSize(5000)
      .expireAfterWrite(60, TimeUnit.MINUTES)
      .build();
  }
  
  @Override
  public Optional<Form> getForm(String formId) {
    return Optional.ofNullable(formCache.getIfPresent(formId));
  }
  
  @Override
  public void putForm(String formId, Form form) {
    if(form != null && formId != null) {
      formCache.put(formId, form);
    }
  }
  
  @Override
  public Optional<FormTag> getFormTag(String formId, String tagName) {
    return Optional.ofNullable(formTagCache.getIfPresent(formId + ":" + tagName));
  }
  
  @Override
  public void putFormTag(String formId, String tagName, FormTag formTag) {
    formTagCache.put(formId + ":" + tagName, formTag);
  }
  
  @Override
  public void evictForm(String formId) {
    formCache.invalidate(formId);
    // Evict all tags for this form
    formTagCache.asMap().keySet().removeIf(key -> key.startsWith(formId + ":"));
  }
  
  @Override
  public void evictAll() {
    formCache.invalidateAll();
    formTagCache.invalidateAll();
  }
}
