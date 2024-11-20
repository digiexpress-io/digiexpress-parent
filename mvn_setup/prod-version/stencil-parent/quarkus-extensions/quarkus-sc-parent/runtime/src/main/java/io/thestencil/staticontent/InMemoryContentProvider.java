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

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class InMemoryContentProvider implements ContentProvider {

  private final Map<String, String> contentValue;

  private final String defaultLocale;

  public InMemoryContentProvider(
      Map<String, String> contentValue,
      String defaultLocale) {
    super();
    this.contentValue = contentValue;
    this.defaultLocale = defaultLocale;
  }

  public String getContentValue(String queryLocale) {
    final String usedLocale;
    if(queryLocale != null && contentValue.containsKey(queryLocale)) {
      usedLocale = queryLocale;  
    } else {
      usedLocale = defaultLocale;      
    }
    final String result = contentValue.get(usedLocale);
    
    log.debug("STATIC CONTENT query, query locale: '{}', used locale: '{}'", queryLocale, usedLocale);
    return result;
  }
}
