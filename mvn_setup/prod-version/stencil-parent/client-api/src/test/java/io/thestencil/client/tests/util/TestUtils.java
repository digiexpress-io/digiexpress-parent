package io.thestencil.client.tests.util;

/*-
 * #%L
 * stencil-static-content
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.tests.StaticContentSiteTest;

public class TestUtils {

  
  public static SiteState getSite(String fileName) {
    try {
      String file = toString(fileName);
      return PgTestTemplate.objectMapper.readValue(file, SiteState.class);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  

  public static String prettyPrint(Object value) {
    try {
      return PgTestTemplate.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  public static String toString(String resource) {
    try {
      return IOUtils.toString(StaticContentSiteTest.class.getClassLoader().getResource(resource), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
