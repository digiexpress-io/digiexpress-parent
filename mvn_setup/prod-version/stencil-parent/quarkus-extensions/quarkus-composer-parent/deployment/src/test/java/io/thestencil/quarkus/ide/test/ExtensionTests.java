package io.thestencil.quarkus.ide.test;

/*-
 * #%L
 * quarkus-stencil-ide-deployment
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

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.nio.file.Path;

import static org.hamcrest.Matchers.*;

public class ExtensionTests {
  @RegisterExtension
  final static QuarkusUnitTest config = new QuarkusUnitTest()
    .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
      .addAsResource(new StringAsset("""
        quarkus.stencil-composer.server-path=PM
        """),
        "application.properties")
    );

  @Test
  public void getUIOnRoot() {
    RestAssured.when().get("/portal-app").then().statusCode(200).body(containsString("url: '/PM'"));
    RestAssured.when().get("/portal-app/?test").then().statusCode(200).body(containsString("url: '/PM'"));
    ValidatableResponse response = RestAssured.when().get("portal-app").then().statusCode(200);
    response.body(containsString("url: '/PM'"));
    var scriptSrc = response.extract().body().htmlPath().getString("**.findAll { it.@type == 'module' }.@src");
    scriptSrc = Path.of("/portal-app/" + scriptSrc).normalize().toString();
    // Assert we do not get html page...
    RestAssured.when().get(scriptSrc).then().statusCode(200).body(not(startsWith("<!DOCTYPE html>")));
  }

  @Test
  public void getUIOnSubPath() {
    RestAssured.when().get("/portal-app/view/sub").then().statusCode(200).body(allOf(
            startsWith("<!DOCTYPE html>"),
            containsString("url: '/PM'")));
    RestAssured.when().get("/portal-app/view/sub/").then().statusCode(200).body(allOf(
            startsWith("<!DOCTYPE html>"),
            containsString("url: '/PM'")));
  }

  @Test
  public void getManifestJson() {
    RestAssured.when().get("/portal-app/manifest.json").then().statusCode(200).body(allOf(
            containsString("background_color")));
  }

  @Test
  public void getRobotsTxt() {
    RestAssured.when().get("/portal-app/robots.txt").then().statusCode(200).body(allOf(
            containsString("User-agent")));
  }

  @Test
  public void getNonExisting() {
    RestAssured.when().get("/portal-app/non-existent.html").then().statusCode(404).body(allOf(
            containsString("Resource not found")));
  }

  @Test
  public void getUIOnQ() {
    RestAssured.when().get("q/portal-app").then().statusCode(404);
  }
}
