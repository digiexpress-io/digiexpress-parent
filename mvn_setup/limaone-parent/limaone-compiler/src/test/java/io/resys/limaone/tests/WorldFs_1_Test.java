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

import org.junit.jupiter.api.Test;

import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.model.ImmutableLocaleLabel;
import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.tests.support.DbSupport;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class WorldFs_1_Test extends DbSupport {

  public WorldFs_1_Test() {
    super();
    this.tenantName = "assets";
    this.createInit = false;
  }

  @Test
  public void createSimpleFileSystem() {
    final var authoring = new AuthoringImpl(createConfig());
    createLocalesArticlesLinks(authoring);
    
    
    final var fileSystem = authoring.worldFsQuery().findAllSync();
    log.debug("Current FS: {}", JsonObject.mapFrom(fileSystem).encodePrettily());
    
    log.debug("Current FS as paths:\n{}", String
        .join("\n", fileSystem.getDirents()
        .stream()
        .map(e -> " - " + e.getFullPath())
        .sorted((a, b) -> a.toLowerCase().compareTo(b.toLowerCase()))
        .toList()));
  }
  
  
  private void createLocalesArticlesLinks(Authoring authoring) {
    if(!createInit) {
      return;
    }
    final var locale1 = authoring.newModel()
        .newLocale()
        .props(props -> props.locale("en"))
        .buildSync();
    
    final var locale2 = authoring.newModel()
        .newLocale()
        .props(props -> props.locale("fi"))
        .buildSync();
    
    final var article1 = authoring.newModel()
        .newArticle()
        .props(builder -> builder.name("My first article").order(100))
        .buildSync();
    
    final var article2 = authoring.newModel()
        .newArticle()
        .props(builder -> builder.name("My second article").order(100).parentId(article1.getId()))
        .buildSync();
    
    final var page1 = authoring.newModel()
        .newArticlePage()
        .props(props -> props.articleId(article1.getId()).locale(locale1.getId()).content("# English content"))
        .buildSync();
    
    final var page2 = authoring.newModel()
      .newArticlePage()
      .props(props -> props.articleId(article1.getId()).locale(locale2.getId()).content("# Finnish content"))
      .buildSync();
    
    final var page3 = authoring.newModel()
        .newArticlePage()
        .props(props -> props.articleId(article2.getId()).locale(locale2.getId()).content("# Finnish content"))
        .buildSync();
      
    final var template1 = authoring.newModel()
      .newArticleTemplate()
      .props(props -> props.name("Template1").content("#Heading1").description("Very good template1").type("type"))
      .buildSync();
    
    final var template2 = authoring.newModel()
        .newArticleTemplate()
        .props(props -> props.name("Template2").content("#Heading1").description("Excellent template2").type("type"))
        .buildSync();
      
    
    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(props -> props.serviceName("Printout1").orchestratorName("PrintoutFlow"))
        .buildSync();
      
    final var link1 = authoring.newModel()
        .newArticleLink().props(props -> props.type("internal").value("www.link1.com")
        .addLabels(ImmutableLocaleLabel.builder()
            .locale(locale1.getId()).labelValue("click me")
            .build())
        .addArticles(article1.getId())
      ).buildSync();
    
    final var workflow1 = authoring.newModel()
        .newArticleWorkflow().props(props -> props.value("Workflow1")
          .formName("form1").formTag("v1").flowName("flow1").formId("external-form-id")
          .addLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("firstForm").build())
          .build()
      ).buildSync();
  }
}
