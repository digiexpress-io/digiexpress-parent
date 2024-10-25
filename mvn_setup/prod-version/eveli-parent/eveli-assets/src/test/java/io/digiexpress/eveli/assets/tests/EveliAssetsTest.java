package io.digiexpress.eveli.assets.tests;

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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.digiexpress.eveli.assets.api.ImmutableCreatePublication;
import io.digiexpress.eveli.assets.api.ImmutableCreateWorkflow;
import io.digiexpress.eveli.assets.api.ImmutableCreateWorkflowTag;
import io.digiexpress.eveli.assets.tests.util.PgProfile;
import io.digiexpress.eveli.assets.tests.util.PgTestTemplate;
import io.digiexpress.eveli.assets.tests.util.TestExporter;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;






@QuarkusTest
@TestProfile(PgProfile.class)
public class EveliAssetsTest extends PgTestTemplate {
  

  @Test
  public void test1() {
    final var repo = getPersistence("test1");
    
    final var wk1 = repo.create().workflow(
        ImmutableCreateWorkflow.builder().name("case-managment-1").formName("fill-questions-for-feedback").formTag("v#1").flowName("create-task-wrench-flow").build()
    ).onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
        
  
    final var wk2 = repo.create().workflow(
        ImmutableCreateWorkflow.builder().name("case-managment-2").formName("general-complaint").formTag("v#4").flowName("create-task-wrench-flow").build()
    ).onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
        
    final var tag = repo.create().workflowTag(
        ImmutableCreateWorkflowTag.builder().name("backup-1").description("release candidate#1").build()
    ).onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
    
    final var pub = repo.create().publication(
        ImmutableCreatePublication.builder().name("first production")
        .stencilTag("stencil#1")
        .wrenchTag("stencil#2")
        .workflowTag(tag.getBody().getName())
        .build()
    ).onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));
          
    
    // find workflow by name
    Assertions.assertEquals(wk2.getId(), repo.getClient().queryBuilder().findOneWorkflowByName(wk2.getBody().getName()).await().atMost(Duration.ofMinutes(1)).get().getId());
    
    // create state
    var expected = TestExporter.toString(getClass(), "create_state.txt");
    var actual = super.toRepoExport("test1");
    Assertions.assertEquals(expected, actual);
    
    /*
    repo.update().template(ImmutableTemplateMutator.builder().templateId(template1.getId())
    	.name("new name")
    	.content("cool content")
    	.type("PAGE")
    	.description("description")
    	.build())
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));

    
    // update state
    expected = TestExporter.toString(getClass(), "update_state.txt");
    actual = super.toRepoExport("test1");
    Assertions.assertEquals(expected, actual);
    
    repo.delete().template(template1.getId())
    	  .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull().await().atMost(Duration.ofMinutes(1));

    // delete state
    expected = TestExporter.toString(getClass(), "delete_state.txt");
    actual = super.toRepoExport("test1");
    Assertions.assertEquals(expected, actual);
    */
    
  }
}
