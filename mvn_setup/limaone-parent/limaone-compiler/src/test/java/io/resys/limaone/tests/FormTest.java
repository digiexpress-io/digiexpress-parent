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

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.dialob.api.form.Form;
import io.dialob.api.questionnaire.Questionnaire;
import io.resys.limaone.tests.support.TestTemplate;
import io.resys.thena.test.DialobTest;
import io.resys.thena.test.DialobTest.DialobResetDB;
import io.resys.thena.test.DialobTest.FormUrl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;



@DialobTest( enabled = true )
public class FormTest {

  @SuppressWarnings("unused")
  @Test @DialobResetDB
  public void test(FormUrl formUrl) {
    final var formDb = TestTemplate.getFormDb(formUrl);
    final var form = new JsonObject(TestTemplate.toString("forms/palaute.json")).mapTo(Form.class);
    
    final var created = formDb.withTenant().createForm()
      .props(form).build()
      .await().atMost(Duration.ofMinutes(1));
    
    final var merged = formDb.withTenant().mergeForm()
      .props(created).build()
      .await().atMost(Duration.ofMinutes(1));
    
    final var tag = formDb.withTenant().createFormTag()
        .formName(created.getName()).formVersion("my-first-tag")
        .build()
        .await().atMost(Duration.ofMinutes(1));
    
    final var tags = formDb.withTenant()
        .formTagQuery().findAll()
        .collect().asList()
        .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(1, tags.size(), "Expected exactly 1 created tag");
    
    
    // form queries
    {
      final var form_byId = formDb.withTenant()
          .formQuery().formId(created.getId()).findOne()
          .await().atMost(Duration.ofMinutes(1));
      Assertions.assertTrue(form_byId.isPresent(), "Expected exactly 1 created form by technical id");
    
      
      final var formByTag = formDb.withTenant()
        .formQuery().formTag(created.getName(), "my-first-tag").findOne()
        .await().atMost(Duration.ofMinutes(1));
      Assertions.assertTrue(formByTag.isPresent(), "Expected exactly 1 created form by name and tag");
      
      
      final var allForms = formDb.withTenant()
          .formMetaQuery().findAll().collect().asList()
          .await().atMost(Duration.ofMinutes(1));
      Assertions.assertTrue(formByTag.isPresent(), "Expected exactly 1 created form");
    }
    
    // form queries without cache
    {
      formDb.getFormDbProps().getCache().evictAll();
      final var form_byId = formDb.withTenant()
          .formQuery().formId(created.getId()).findOne()
          .await().atMost(Duration.ofMinutes(1));
      Assertions.assertTrue(form_byId.isPresent(), "Expected exactly 1 created form by technical id");
      
      
      formDb.getFormDbProps().getCache().evictAll();
      final var formByTag = formDb.withTenant()
        .formQuery().formTag(created.getName(), "my-first-tag").findOne()
        .await().atMost(Duration.ofMinutes(1));
      Assertions.assertTrue(formByTag.isPresent(), "Expected exactly 1 created form by name and tag");
      
      
      formDb.getFormDbProps().getCache().evictAll();
      final var allForms = formDb.withTenant()
          .formMetaQuery().findAll().collect().asList()
          .await().atMost(Duration.ofMinutes(1));
      Assertions.assertTrue(formByTag.isPresent(), "Expected exactly 1 created form");
    }
    
    // create for session
    {
      final var allForms = formDb.withTenant()
          .formMetaQuery().findAll().collect().asList()
          .await().atMost(Duration.ofMinutes(1));
      
      final var sessionId = formDb.withTenant().createFormInstance()
        .formId(allForms.iterator().next().getId())
        .build()
        .await().atMost(Duration.ofMinutes(1));
      
      final var initSession = formDb.withTenant().formFillQuery().getOne(sessionId.getId())
        .await().atMost(Duration.ofMinutes(1));
      
      Assertions.assertTrue(initSession.isOk(), "Expected Ok for created session");
      Assertions.assertNotNull(initSession.getBody(), "Expected Ok for created session");
      
      final var formFilled = formDb.withTenant().createFormFill()
        .formInstanceId(sessionId.getId())
        .actions(JsonObject.of(
            "rev", sessionId.getRev(), 
            "actions", new JsonArray(
"""
[
  {'type':'ANSWER','answer':'no','id':'authentication'},
  {'type':'ANSWER','answer':'cityService','id':'mainList'},
  {'type':'ANSWER','answer':'info','id':'cityServiceMainList'},
  {'type':'ANSWER','answer':'thanks','id':'typeOfFeedback'},
  {'type':'ANSWER','answer':'thank you','id':'feedBackTitle'},
  {'type':'ANSWER','answer':'very big text','id':'feedBackTxt'},
  {'type':'ANSWER','answer':false,'id':'boolean11'},
  {'type':'COMPLETE'}
]""".replace("'", "\""))).encode())
        .build()
        .await().atMost(Duration.ofMinutes(1));
        
      Assertions.assertTrue(formFilled.isOk(), "Expected Ok for filled session");
      
      
      final var completedSession = formDb.withTenant().formInstanceQuery().getOne(sessionId.getId())
          .await().atMost(Duration.ofMinutes(1));
      Assertions.assertEquals(
          Questionnaire.Metadata.Status.COMPLETED,
          completedSession.getQuestionnaire().getMetadata().getStatus(), 
          "Expected Ok for filled session");
    } 
  }

  
  


  @SuppressWarnings("unused")
  @Test @DialobResetDB
  public void reviewTest(FormUrl formUrl) {
    final var formDb = TestTemplate.getFormDb(formUrl);  
    final var form_2 = new JsonObject(TestTemplate.toString("forms/osbu_proposals.json")).mapTo(Form.class);
    final var created_2 = formDb.withTenant().createForm()
      .props(form_2).build()
      .await().atMost(Duration.ofMinutes(1));
    

    final var sessionId = formDb.withTenant().createFormInstance()
      .formId(created_2.getId())
      .context(Map.of("SocialSecurityNumber", "123456-789A"))
      .build()
      .await().atMost(Duration.ofMinutes(1));
    

      
      final var initSession = formDb.withTenant().formFillQuery().getOne(sessionId.getId())
        .await().atMost(Duration.ofMinutes(1));
      Assertions.assertTrue(initSession.isOk(), "Expected Ok for created session");
      Assertions.assertNotNull(initSession.getBody(), "Expected Ok for created session");
      
    
    
    
    final var formFilled = formDb.withTenant().createFormFill()
        .formInstanceId(sessionId.getId())
        .actions(JsonObject.of(
            "rev", sessionId.getRev(), 
            "actions", new JsonArray(
"""
[
  {'type':'ANSWER','answer':'choice1','id':'list1'},
  {'type':'ANSWER','answer':'Mekaanikonkatu, 00880, Helsinki','id':'address1'}
]""".replace("'", "\""))).encode())
        .build()
        .await().atMost(Duration.ofMinutes(1));
    
    final var nextRev = formDb.withTenant().createFormFill()
    .formInstanceId(sessionId.getId())
    .actions(JsonObject.of(
        "rev", new JsonObject(formFilled.getBody()).getString("rev"), 
        "actions", new JsonArray("[{'type':'COMPLETE'}]".replace("'", "\""))).encode())
    .build()
    .await().atMost(Duration.ofMinutes(1));
    
    final var completedSession = formDb.withTenant().formInstanceQuery().getOne(sessionId.getId())
        .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(
        Questionnaire.Metadata.Status.COMPLETED,
        completedSession.getQuestionnaire().getMetadata().getStatus(), 
        "Expected Ok for filled session:\n" + JsonObject.mapFrom(completedSession.getQuestionnaire()).encodePrettily());

    
    final var actions = formDb.withTenant()
        .formFillReview()
        .formInstanceId(completedSession.getQuestionnaire().getId())
        .build()
        .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(12, actions.getActions().size());
    Assertions.assertEquals(1, 
        actions.getActions().stream()
          .filter(e -> e.getItem() != null)
          .filter(e -> e.getItem().getId() != null)
          .filter(e -> e.getItem().getId().equals("questionnaire"))
          .count());
          
  }
}
