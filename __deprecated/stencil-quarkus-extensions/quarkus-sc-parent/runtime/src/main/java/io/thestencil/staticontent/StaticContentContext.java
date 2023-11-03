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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.thestencil.client.api.MigrationBuilder.Sites;

public class StaticContentContext {
  private static final Logger LOGGER = LoggerFactory.getLogger(StaticContentContext.class.getName());
  
  private final Sites content;
  private final Map<String, String> contentValue;
  private final String defaultLocale;
  public StaticContentContext(
      Sites content, 
      Map<String, String> contentValue,
      String defaultLocale) {
    super();
    this.content = content;
    this.contentValue = contentValue;
    this.defaultLocale = defaultLocale;
  }
  public Sites getContent() {
    return content;
  }
  public String getContentValue(String queryLocale) {
    final String usedLocale;
    if(queryLocale != null && contentValue.containsKey(queryLocale)) {
      usedLocale = queryLocale;  
    } else {
      usedLocale = defaultLocale;      
    }
    final String result = contentValue.get(usedLocale);
    
    if(LOGGER.isDebugEnabled()) {
      LOGGER.debug("STATIC CONTENT query,"
          + " query locale: '" + queryLocale + "',"
          + " used locale: '" + usedLocale + "'");
    }
    return result;
  }
}
