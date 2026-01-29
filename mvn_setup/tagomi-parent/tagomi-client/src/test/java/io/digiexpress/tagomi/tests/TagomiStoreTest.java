package io.digiexpress.tagomi.tests;

/*-
 * #%L
 * tagomi-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import io.digiexpress.tagomi.api.commands.ImmutableCreateLocale;
import io.digiexpress.tagomi.api.commands.ImmutableCreateResource;
import io.digiexpress.tagomi.api.commands.ImmutableCreateService;
import io.digiexpress.tagomi.api.commands.ImmutableCreateTag;
import io.digiexpress.tagomi.api.commands.ImmutableCreateTemplate;
import io.digiexpress.tagomi.api.commands.ImmutableLocaleMutator;
import io.digiexpress.tagomi.api.commands.ImmutableResourceMutator;
import io.digiexpress.tagomi.api.commands.ImmutableTemplateMutator;
import io.digiexpress.tagomi.api.entities.TagomiContainer.ResourceType;
import io.digiexpress.tagomi.tests.config.PgTestTemplate;


public class TagomiStoreTest extends PgTestTemplate {
  

  @Test
  public void test1() {
    final var repo = createTenant("test1");
    
   final var article1 = repo.create().service(
        ImmutableCreateService.builder().serviceName("My first article").orchestratorName("").build()
    )   .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));

   final var article2 = repo.create().service(
       ImmutableCreateService.builder().serviceName("My second article").orchestratorName("").build()
    )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
   repo.create().tag(
       ImmutableCreateTag.builder()
       .tagName("v1.5").note("test release")
       .build()
    )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
   
   repo.create().tag(
       ImmutableCreateTag.builder()
         .tagName("v2.4")
         .note("new content")
         .build()
    )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
   
   final var locale1 = repo.create().locale(
        ImmutableCreateLocale.builder().localeCode("en").build()
      )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    final var locale2 = repo.create().locale(
        ImmutableCreateLocale.builder().localeCode("fi").build()
      ).await().atMost(Duration.ofMinutes(1));
    
    final var page1 = repo.create().template(
        ImmutableCreateTemplate.builder().serviceId(article1.getId()).locale(locale1.getId()).content("# English content").build()
      )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.create().template(
        ImmutableCreateTemplate.builder().serviceId(article1.getId()).locale(locale2.getId()).content("# Finnish content").build()
      )      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    final var link1 = repo.create().resource(
        ImmutableCreateResource.builder()
          .resourceName("super_image_1")
          .contentType(ResourceType.LOGO)
          .uploadBody(new byte[] {})
        .build()
      ).onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    final var link2 = repo.create().resource(
        ImmutableCreateResource.builder()
        .resourceName("super_script_1")
        .contentType(ResourceType.SCRIPT)
        .uploadBody(new byte[] {})
      .build()
      ).onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    // create state
    var expected = toString("create_state.txt");
    var actual = super.toRepoExport("test1");
    Assertions.assertEquals(expected, actual);
    
    repo.update().template(ImmutableTemplateMutator.builder().templateId(page1.getId())
      .content("cool content")
      .build())
      .onFailure().invoke(e -> e.printStackTrace())
      .await().atMost(Duration.ofMinutes(1));

    
    repo.update().locale(ImmutableLocaleMutator.builder().localeId(locale1.getId()).value("gb").enabled(false).build())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.update().template(ImmutableTemplateMutator.builder()
          .templateId(page1.getId())
          .content("new content for page1")
          .locale(locale1.getId()).build())
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.update().resource(ImmutableResourceMutator.builder()
          .resourceId(link1.getId())
          .templateIds(Arrays.asList(page1.getId()))
          .resourceName("better_image_name")
          .build())
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.update().resource(ImmutableResourceMutator.builder()
        .resourceId(link2.getId())
        .templateIds(Arrays.asList(page1.getId()))
        .resourceName("better_image_name_2")
        .build())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    
    // update state
    expected = toString("update_state.txt");
    actual = super.toRepoExport("test1");
    Assertions.assertEquals(expected, actual);

    repo.delete().template(page1.getId())
        .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));

    
    repo.delete().service(article1.getId())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.delete().service(article2.getId())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.delete().locale(locale1.getId())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.delete().resource(link1.getId())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    repo.delete().resource(link2.getId())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    // delete state
    expected = toString("delete_state.txt");
    actual = super.toRepoExport("test1");
    Assertions.assertEquals(expected, actual);
  }
}
