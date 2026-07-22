package io.resys.limaone.tests;

import org.junit.jupiter.api.Assertions;

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

import org.junit.jupiter.api.Test;

import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.tests.support.DbSupport;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Authoring_9_Copy_Test extends DbSupport {

  
  @Test
  public void stencilAssetTest() {
    final var authoring = new AuthoringImpl(createConfig());
    
    final var template1 = authoring.newModel()
        .newArticleTemplate()
        .props(props -> props.name("Nice page template").content("# Header 1").type("Page"))
        .buildSync();
        
    final var article1 = authoring.newModel()
        .newArticle()
        .props(builder -> builder.name("My first article").order(100))
        .buildSync();
    
    final var copied = authoring.copyAsModel().copyAny().props(props -> props.idOfObjectToCopy(article1.getId()).newObjectName("copied article 1")).buildSync();
    
    {
      final var fs = authoring.worldFsQuery().findAllSync().flatAll();
      final var actual = fs.stream().filter(e -> e.getId().equals(copied.getId())).findFirst();
      log.debug("copied: {}", actual);
      
      Assertions.assertEquals(true, actual.isPresent());
    }
  }
}
