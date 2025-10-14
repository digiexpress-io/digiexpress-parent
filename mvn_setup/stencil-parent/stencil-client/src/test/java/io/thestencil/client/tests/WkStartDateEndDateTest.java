package io.thestencil.client.tests;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/*-
 * #%L
 * stencil-persistence
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

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.thestencil.client.api.ImmutableCreateArticle;
import io.thestencil.client.api.ImmutableCreateLocale;
import io.thestencil.client.api.ImmutableCreatePage;
import io.thestencil.client.api.ImmutableCreateTemplate;
import io.thestencil.client.api.ImmutableCreateWorkflow;
import io.thestencil.client.api.ImmutableLocaleLabel;
import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.Locale;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.Template;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.spi.builders.StencilEnvirImpl;
import io.thestencil.client.tests.util.PgProfile;
import io.thestencil.client.tests.util.PgTestTemplate;

@QuarkusTest
@TestProfile(PgProfile.class)
public class WkStartDateEndDateTest extends PgTestTemplate {
  

  @Test
  public void test1() {
    final var repo = getPersistence("test3-customId");
    
    Entity<Template> template1 = repo.create().template(
        ImmutableCreateTemplate.builder().id("1").name("Nice page template").content("# Header 1").type("Page").description("Generic page structure").build()
    ).onFailure().invoke(Throwable::printStackTrace).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
        
   Entity<Article> article1 = repo.create().article(
        ImmutableCreateArticle.builder().id("2").name("My first article").order(100).build()
    )      .onFailure().invoke(Throwable::printStackTrace).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));

   Entity<Article> article2 = repo.create().article(
        ImmutableCreateArticle.builder().id("3").name("My second article").order(100).build()
    )      .onFailure().invoke(Throwable::printStackTrace).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    Entity<Locale> locale1 = repo.create().locale(
        ImmutableCreateLocale.builder().id("6").locale("en").build()
      )      .onFailure().invoke(Throwable::printStackTrace).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    Entity<Page> page1 = repo.create().page(
        ImmutableCreatePage.builder().articleId(article1.getId()).locale(locale1.getId()).content("# English content").build()
      )      .onFailure().invoke(Throwable::printStackTrace).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    
    Entity<Workflow> workflow1 = repo.create().workflow( 
        ImmutableCreateWorkflow.builder().value("Form1")
          .formName("form1").formTag("v1").flowName("flow1").formId("external-form-id")
          .startDate(LocalDateTime.of(2025, 02, 01, 06, 00))
          .endDate(LocalDateTime.of(2025, 03, 01, 05, 59))
          .addLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("firstForm").build())
          .build()
      )      .onFailure().invoke(Throwable::printStackTrace).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    
    
    final var state = repo.query().head().await().atMost(Duration.ofMinutes(1));
    final var envir = StencilEnvirImpl.of(state, "dev", true);

    // one second too early for workflow
    Assertions.assertEquals(0, 
        envir.get(OffsetDateTime.of(LocalDateTime.of(2025, 02, 01, 05, 59), ZoneOffset.UTC), false)
        .getSites().get("en").getLinks().size()
    );
    
    Assertions.assertEquals(1, 
        envir.get(OffsetDateTime.of(LocalDateTime.of(2025, 02, 01, 06, 00), ZoneOffset.UTC), false)
        .getSites().get("en").getLinks().size()
    );
    
    Assertions.assertEquals(1, 
        envir.get(OffsetDateTime.of(LocalDateTime.of(2025, 02, 01, 07, 00), ZoneOffset.UTC), false)
        .getSites().get("en").getLinks().size());
    
    Assertions.assertEquals(1, 
        envir.get(OffsetDateTime.of(LocalDateTime.of(2025, 03, 01, 05, 59), ZoneOffset.UTC), false)
        .getSites().get("en").getLinks().size()
    );
    
    Assertions.assertEquals(0, 
        envir.get(OffsetDateTime.of(LocalDateTime.of(2025, 03, 01, 06, 00), ZoneOffset.UTC), false)
        .getSites().get("en").getLinks().size()
    );
  }
}
