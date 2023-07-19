package io.thestencil.client.tests;

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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.thestencil.client.api.StencilClient;
import io.thestencil.client.spi.StencilClientImpl;
import io.thestencil.client.tests.util.PgTestTemplate;
import io.thestencil.client.tests.util.TestUtils;

public class StaticContentSiteTest {  
  final StencilClient client = StencilClientImpl.builder().config(c -> c.objectMapper(PgTestTemplate.objectMapper)).inmemory().build();
  
  @Test
  public void buildSiteForProd() {
    final var md = client.markdown().json(TestUtils.toString("site.json"), false)
        .build();

    final var content = client
        .sites().imagePath("/images").created(1l)
        .source(md)
        .build();
    
    String expected = TestUtils.toString("content.json");
    String actual = TestUtils.prettyPrint(content);
    Assertions.assertEquals(expected, actual); 
  }
  @Test
  public void buildSiteForDev() {
    final var md = client.markdown().json(TestUtils.toString("site.json"), true)
        .build();

    final var content = client
        .sites().imagePath("/images").created(1l)
        .source(md)
        .build();
    
    String expected = TestUtils.toString("contentDevMode.json");
    String actual = TestUtils.prettyPrint(content);
    Assertions.assertEquals(expected, actual); 
  }
  

}
