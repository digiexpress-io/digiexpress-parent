package io.resys.limaone.tests;

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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.limaone.model.Article;
import io.resys.limaone.model.ImmutableDescriptionLabel;
import io.resys.limaone.model.Model;
import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.tests.support.DbSupport;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Authoring_8_Meta_Test extends DbSupport {

  @Test
  @SuppressWarnings("unchecked")
  public void stencilAssetLabelAndDescTest() {
    final var authoring = new AuthoringImpl(createConfig());
    
    final var template1 = authoring.newModel()
        .newArticleTemplate()
        .props(props -> props.name("Nice page template").content("# Header 1").type("Page"))
        .buildSync();
        

    final var article1 = authoring.newModel()
        .newArticle()
        .props(builder -> builder.name("My first article").order(100))
        .buildSync();

    final Model<Article> renamedArticle = (Model<Article>) authoring.modifyModel().modifyAssetName()
      .props(props -> props.id(article1.getId()).name("Renamed article1"))
      .buildSync();
    
    Assertions.assertEquals("Renamed article1", renamedArticle.getBody().getName());
    
    {
      final var fs = authoring.worldFsQuery().findAllSync().flatAll();
      final var found = fs.stream().filter(t -> t.getId().equals(article1.getId())).findFirst().get();
      Assertions.assertEquals("articles/Renamed article1/article", found.getFullPath());    
    }
    
    authoring.modifyModel().modifyDescription()
      .props(props -> props.id(template1.getId()).text("text text 1"))
      .buildSync();
    
    authoring.modifyModel().modifyLabels()
      .props(props -> props.id(template1.getId()).addValues(ImmutableDescriptionLabel.builder()
          .key("mundo")
          .text("un")
          .build()))
      .buildSync();
    

    final var described = authoring.worldFsQuery().findAllSync().getDirents().stream()
      .flatMap(e -> e.getChildren().stream())
      .filter(d -> d.getId().equals(template1.getId()))
      .findFirst().orElseThrow();
    Assertions.assertEquals("text text 1", described.getProps().getAssetDescription());    
    Assertions.assertEquals("[DescriptionLabel{key=mundo, text=un}]", described.getProps().getLabels().toString());    
  }
}
