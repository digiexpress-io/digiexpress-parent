package io.thestencil.quarkus.ide.services.tests;

import static org.hamcrest.Matchers.matchesPattern;

/*-
 * #%L
 * quarkus-stencil-ide-services-deployment
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

import java.util.Arrays;

import org.hamcrest.Matchers;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import io.thestencil.client.api.ImmutableArticle;
import io.thestencil.client.api.ImmutableArticleMutator;
import io.thestencil.client.api.ImmutableCreateArticle;
import io.thestencil.client.api.ImmutableCreateLink;
import io.thestencil.client.api.ImmutableCreateLocale;
import io.thestencil.client.api.ImmutableCreatePage;
import io.thestencil.client.api.ImmutableCreateRelease;
import io.thestencil.client.api.ImmutableCreateWorkflow;
import io.thestencil.client.api.ImmutableEntity;
import io.thestencil.client.api.ImmutableLinkMutator;
import io.thestencil.client.api.ImmutableLocaleLabel;
import io.thestencil.client.api.ImmutableLocaleMutator;
import io.thestencil.client.api.ImmutablePageMutator;
import io.thestencil.client.api.ImmutableWorkflowMutator;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


//-Djava.util.logging.manager=org.jboss.logmanager.LogManager
public class IdeServicesTests extends PgSqlDbConfig {
  @RegisterExtension
  final static QuarkusUnitTest config = new QuarkusUnitTest()
    .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
      .addAsResource(new StringAsset(
          "quarkus.stencil-composer-pg.repo.repo-name=test-assets\r\n" +
          "quarkus.stencil-composer-pg.service-path=stencil-ide-services\r\n"
          ), "application.properties")
    );

  @Test
  public void releaseVersionAndDateTest() throws JsonProcessingException {
    RestAssured.given()
      .when()
        .get("/stencil-ide-services/version")
      .then()
            .contentType("application/json")
            .statusCode(200)
            .assertThat().body("version", matchesPattern("\\d+\\.\\d+\\.\\d+"))
            .assertThat().body("date", matchesPattern("\\d{2}/\\d{2}/\\d{4}"));
  }
  
  @Test
  public void postArticles() {

    RestAssured.given()
    .when().get("/stencil-ide-services/")
    .then().statusCode(200);
    
    RestAssured.given()
    .when().post("/stencil-ide-services")
    .then().statusCode(200);
    
    
    String localeId = RestAssured.given()
        .body( 
            JsonObject.mapFrom(
                ImmutableCreateLocale.builder()
                .locale("en")
                .build()
                ).toString())
              .when().post("/stencil-ide-services/locales")
              .then().statusCode(200)
                .extract()
                .response()
                .body()
                .path("id");
        
    
    final var createdArticle = RestAssured.given()
    .body(
        JsonObject.mapFrom(
          ImmutableCreateArticle.builder()
          .name("test-article")
          .build()
        ).toString())
    .when().post("/stencil-ide-services/articles")
    .then().statusCode(200)
    .extract()
      .response()
      .body();
    
   String articleId = createdArticle.path("id");
   
   Assertions.assertNull(createdArticle.path("body.devMode"));
  
   
    
   String pageId = RestAssured.given()
      .body(
          JsonObject.mapFrom(
              ImmutableCreatePage.builder()
              .locale(localeId)
              .content("# Header 1")
              .articleId(articleId)
              .build()
              ).toString())
            .when().post("/stencil-ide-services/pages")
            .then().statusCode(200)
              .extract()
              .response()
              .body()
              .path("id");
    
   String linkId = RestAssured.given()
    .body(
        JsonObject.mapFrom(
            ImmutableCreateLink.builder()
            .addLabels(ImmutableLocaleLabel.builder()
                .locale(localeId)
                .labelValue("description")
                .build())
            .value("www.example.com")
            .type("internal")
            .addArticles(articleId)
            .build()
            ).toString())
          .when().post("/stencil-ide-services/links")
          .then().statusCode(200)
            .extract()
            .response()
            .body()
            .path("id");
  
   String workflowId = RestAssured.given() 
    .body(
        JsonObject.mapFrom(
            ImmutableCreateWorkflow.builder()
            .addLabels(ImmutableLocaleLabel.builder()
                .locale(localeId)
                .labelValue("cool name")
                .build())
            .value("workflow name")
            .build()
            ).toString())
          .when().post("/stencil-ide-services/workflows")
          .then().statusCode(200)
            .extract()
            .response()
            .body()
            .path("id");
   

   String releaseId = RestAssured.given()
   .body(
       JsonObject.mapFrom(
           ImmutableCreateRelease.builder()
           .name("v1.0")
           .note("init release")
           .build()
           ).toString())
         .when().post("/stencil-ide-services/releases")
         .then().statusCode(200)
         .extract()
         .response()
         .body()
         .path("id");
   
   Response release = RestAssured.given().when().get("/stencil-ide-services/releases/"+releaseId);
   release.prettyPrint();
   release.then().statusCode(200);
   
   
   /* ----------------------   UPDATE TESTS  ----------------------*/
   
    RestAssured.given()
    .body(
        JsonObject.mapFrom(
            ImmutableArticleMutator.builder()
            .articleId(articleId)
            .name("new name")
            .order(300)
            .devMode(true)
            .build()
            ).toString())
          .when().put("/stencil-ide-services/articles/")
          .then()
          .statusCode(200)
          .assertThat().body("body.devMode", Matchers.equalTo(true));
    RestAssured.given()
    .body(
        JsonObject.mapFrom(
            ImmutableArticleMutator.builder()
            .articleId(articleId)
            .name("new name")
            .order(300)
            .devMode(null)
            .build()
            ).toString())
          .when().put("/stencil-ide-services/articles/")
          .then()
          .statusCode(200)
          .assertThat().body("body.devMode", Matchers.equalTo(null));
    
    
    RestAssured.given()
    .body(
         new JsonArray(Arrays.asList(ImmutablePageMutator.builder()
             .pageId(pageId)
             .content("# new content")
             .locale(localeId)
             .build())).toString())
          .when().put("/stencil-ide-services/pages")
          .then().statusCode(200);
    
    RestAssured.given()
    .body(
         new JsonArray(Arrays.asList(ImmutablePageMutator.builder()
             .pageId(pageId)
             .content("# new content")
             .locale(localeId)
             .devMode(true)
             .build())).toString())
          .when().put("/stencil-ide-services/pages")
          .then().statusCode(200)
          .assertThat().body("body.devMode[0]", Matchers.equalTo(true));
    
    RestAssured.given()
    .body(
         new JsonArray(Arrays.asList(ImmutablePageMutator.builder()
             .pageId(pageId)
             .content("# new content")
             .locale(localeId)
             .devMode(null)
             .build())).toString())
          .when().put("/stencil-ide-services/pages")
          .then().statusCode(200)
          .assertThat().body("body.devMode[0]", Matchers.equalTo(null));
    
    
    
    RestAssured.given()
    .body(
         JsonObject.mapFrom(
            ImmutableLinkMutator.builder()
            .linkId(linkId)
            .devMode(true)
            .addLabels(ImmutableLocaleLabel.builder()
                .labelValue("# new content")
                .locale(localeId)
                .build())
            .type("internal")
            .value("super duper")
            .build()
            ).toString())
          .when().put("/stencil-ide-services/links")
          .then().statusCode(200)
          .assertThat().body("body.devMode", Matchers.equalTo(true));
    RestAssured.given()
    .body(
         JsonObject.mapFrom(
            ImmutableLinkMutator.builder()
            .linkId(linkId)
            .devMode(null)
            .addLabels(ImmutableLocaleLabel.builder()
                .labelValue("# new content")
                .locale(localeId)
                .build())
            .type("internal")
            .value("super duper")
            .build()
            ).toString())
          .when().put("/stencil-ide-services/links")
          .then().statusCode(200)
          .assertThat().body("body.devMode", Matchers.equalTo(null));
    
    
    RestAssured.given()
    .body(
         JsonObject.mapFrom(
            ImmutableLocaleMutator.builder()
            .localeId(localeId)
            .enabled(true)
            .value("ralru")
            .build()
            ).toString())
          .when().put("/stencil-ide-services/locales")
          .then().statusCode(200);
    
    RestAssured.given()
    .body(
         JsonObject.mapFrom(
            ImmutableWorkflowMutator.builder()
            .workflowId(workflowId)
            .addLabels(ImmutableLocaleLabel.builder()
                .labelValue("updated workflow")
                .locale(localeId)
                .build())
            .value("SuperFlow")
            .build()
            ).toString())
          .when().put("/stencil-ide-services/workflows")
          .then().statusCode(200);

    
    

    Response site = RestAssured.given().when().get("/stencil-ide-services");
    site.prettyPrint();
    site.then().statusCode(200);
    
    // linkArticle
    RestAssured.delete("/stencil-ide-services/links/" + linkId + "?articleId="+articleId)
           .then().statusCode(200);
  
     
     // workflowArticle
    RestAssured.delete("/stencil-ide-services/workflows/" + workflowId + "?articleId="+articleId)
    .then().statusCode(200);    
    
    /* ---------------------- DELETE TESTS  ----------------------*/
  
    // page
    RestAssured.given().delete("/stencil-ide-services/pages/" + pageId)
    .then().statusCode(200);
    
    // article
  
    RestAssured.given().delete("/stencil-ide-services/articles/" + articleId)
    .then().statusCode(200);
    
    
    // link
    
    RestAssured.given().delete("/stencil-ide-services/links/" + linkId)
    .then().statusCode(200);

    
    // locale
    
    RestAssured.given().delete("/stencil-ide-services/locales/" + localeId)
    .then().statusCode(200);
    
    
    // workflow
    
    RestAssured.given().delete("/stencil-ide-services/workflows/" + workflowId)
    .then().statusCode(200);
    
    
  }
  
}
