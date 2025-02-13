package io.thestencil.client.tests;

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

import java.time.Duration;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.thestencil.client.api.ImmutableArticleMutator;
import io.thestencil.client.api.ImmutableCreateArticle;
import io.thestencil.client.api.ImmutableCreateLink;
import io.thestencil.client.api.ImmutableCreateLocale;
import io.thestencil.client.api.ImmutableCreatePage;
import io.thestencil.client.api.ImmutableCreateRelease;
import io.thestencil.client.api.ImmutableCreateTemplate;
import io.thestencil.client.api.ImmutableCreateWorkflow;
import io.thestencil.client.api.ImmutableLinkMutator;
import io.thestencil.client.api.ImmutableLocaleLabel;
import io.thestencil.client.api.ImmutableLocaleMutator;
import io.thestencil.client.api.ImmutablePageMutator;
import io.thestencil.client.api.ImmutableTemplateMutator;
import io.thestencil.client.api.ImmutableWorkflowMutator;
import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.Link;
import io.thestencil.client.api.StencilClient.Locale;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.Template;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.tests.util.PgProfile;
import io.thestencil.client.tests.util.PgTestTemplate;
import io.thestencil.client.tests.util.TestExporter;

@QuarkusTest
@TestProfile(PgProfile.class)
public class PersistencePgTest extends PgTestTemplate {
  

  @Test
  public void test1() {
    final var repo = getPersistence("test1");
    
    Entity<Template> template1 = repo.create().template(
        ImmutableCreateTemplate.builder().name("Nice page template").content("# Header 1").type("Page").description("Generic page structure").build()
    ).onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
        
   Entity<Article> article1 = repo.create().article(
        ImmutableCreateArticle.builder().name("My first article").order(100).build()
    )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));

   Entity<Article> article2 = repo.create().article(
        ImmutableCreateArticle.builder().name("My second article").order(100).build()
    )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
   repo.create().release(
       ImmutableCreateRelease.builder().name("v1.5").note("test release").build()
    )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
   
   repo.create().release(
       ImmutableCreateRelease.builder().name("v2.4").note("new content").build()
    )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
   
    Entity<Locale> locale1 = repo.create().locale(
        ImmutableCreateLocale.builder().locale("en").build()
      )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    Entity<Locale> locale2 = repo.create().locale(
        ImmutableCreateLocale.builder().locale("fi").build()
      ).await().atMost(Duration.ofMinutes(1));
    
    Entity<Page> page1 = repo.create().page(
        ImmutableCreatePage.builder().articleId(article1.getId()).locale(locale1.getId()).content("# English content").build()
      )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.create().page(
        ImmutableCreatePage.builder().articleId(article1.getId()).locale(locale2.getId()).content("# Finnish content").build()
      )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    Entity<Link> link1 = repo.create().link(
        ImmutableCreateLink.builder().type("internal").value("www.example.com")
        .addLabels(ImmutableLocaleLabel.builder()
            .locale(locale1.getId()).labelValue("click me")
            .build())
        .build()
      )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    Entity<Workflow> workflow1 = repo.create().workflow( 
        ImmutableCreateWorkflow.builder().value("Form1")
          .addLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("firstForm").build())
          .build()
      )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    // create state
    var expected = TestExporter.toString(getClass(), "create_state.txt");
    var actual = super.toRepoExport("test1");
    Assertions.assertEquals(expected, actual);
    
    repo.update().template(ImmutableTemplateMutator.builder().templateId(template1.getId())
    	.name("new name")
    	.content("cool content")
    	.type("PAGE")
    	.description("description")
    	.build())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));

    repo.update().article(ImmutableArticleMutator.builder().articleId(article1.getId()).name("Revised Article1").order(300).build())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.update().locale(ImmutableLocaleMutator.builder().localeId(locale1.getId()).value("gb").enabled(false).build())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.update().page(ImmutablePageMutator.builder().pageId(page1.getId()).content("new content for page1").locale(locale1.getId()).build())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.update().link(ImmutableLinkMutator.builder()
          .linkId(link1.getId()).articles(Arrays.asList(article1.getId()))
          .value("www.wikipedia.com").type("external")
          .addLabels(ImmutableLocaleLabel.builder()
              .labelValue("Don't click me").locale(locale2.getId())
              .build())
          .build())
          
    .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.update().workflow(ImmutableWorkflowMutator.builder()
        .workflowId(workflow1.getId())
        .value("revision of firstForm")
        .addLabels(ImmutableLocaleLabel.builder()
            .locale(locale2.getId())
            .labelValue("First form part 2")
            .build())
        .build())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    
    // update state
    expected = TestExporter.toString(getClass(), "update_state.txt");
    actual = super.toRepoExport("test1");
    Assertions.assertEquals(expected, actual);
    
    repo.delete().template(template1.getId())
    	  .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));

    repo.delete().page(page1.getId())
        .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));

    
    repo.delete().article(article1.getId())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.delete().article(article2.getId())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.delete().locale(locale1.getId())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.delete().link(link1.getId())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.delete().workflow(workflow1.getId())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    // delete state
    expected = TestExporter.toString(getClass(), "delete_state.txt");
    actual = super.toRepoExport("test1");
    Assertions.assertEquals(expected, actual);
    
  }
}
