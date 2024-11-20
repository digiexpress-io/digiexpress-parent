package io.thestencil.staticontent.test;

/*-
 * #%L
 * quarkus-stencil-sc-deployment
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
import io.restassured.http.ContentType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Set;

public class SiteJsonExtensionTests {
  @RegisterExtension
  final static QuarkusUnitTest config = new QuarkusUnitTest()
    .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
      .addAsResource(new ClassLoaderAsset("site.json"), "site.json")
      .addAsResource(new StringAsset("""
          quarkus.stencil-sc.site-json=site.json
          quarkus.stencil-sc.service-path=portal/site
          """), "application.properties")
    );

  @Test
  public void shouldGetSiteContentJson() {
    var json = RestAssured.when().get("/portal/site")
            .then().statusCode(200).contentType(ContentType.JSON).extract().body().jsonPath();
    Assertions.assertEquals(Set.of("000_test-article"), json.getJsonObject("topics.keySet()"));
  }

}
