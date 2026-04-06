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

import io.dialob.api.form.Form;
import io.dialob.api.form.FormTag;

/**
 * Cache interface for FormDb operations.
 * Provides caching layer for forms and form tags to reduce HTTP calls.
 */
public interface FormDbCache {
  
  /**
   * Gets a cached form by its technical ID.
   * 
   * @param formId the technical form identifier
   * @return cached form if present, empty otherwise
   */
  Optional<Form> getForm(String formId);
  
  /**
   * Caches a form by its technical ID.
   * 
   * @param formId the technical form identifier
   * @param form the form to cache
   */
  void putForm(String formId, Form form);
  
  /**
   * Gets a cached form tag by form ID and tag name.
   * 
   * @param formId the technical form identifier
   * @param tagName the tag/version name
   * @return cached form tag if present, empty otherwise
   */
  Optional<FormTag> getFormTag(String formId, String tagName);
  
  /**
   * Caches a form tag by form ID and tag name.
   * 
   * @param formId the technical form identifier
   * @param tagName the tag/version name
   * @param formTag the form tag to cache
   */
  void putFormTag(String formId, String tagName, FormTag formTag);
  
  /**
   * Evicts all cached data for a specific form.
   * Use when form is updated or deleted.
   * 
   * @param formId the technical form identifier
   */
  void evictForm(String formId);
  
  /**
   * Evicts all cached data.
   */
  void evictAll();
}
