package io.thestencil.site.pg.test;

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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;
import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.spi.pgsql.PgErrors;
import io.resys.thena.docdb.sql.DocDBFactorySql;


//-Djava.util.logging.manager=org.jboss.logmanager.LogManager
public class SiteExtensionTests {
  @RegisterExtension
  final static QuarkusUnitTest config = new QuarkusUnitTest()
    .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
      .addAsResource(new StringAsset(
          "quarkus.stencil-site-pg.repo.repo-name=test-assets\r\n" +
          "quarkus.stencil-site-pg.service-path=portal/site\r\n"+
          ""), "application.properties")
    );
  
  
  
  @Inject
  io.vertx.mutiny.pgclient.PgPool pgPool;
  private DocDB client;
  
  @BeforeEach
  void startDB() {
    this.setUp();
  }
  private void setUp() {
    this.client = DocDBFactorySql.create()
        .db("junit")
        .client(pgPool)
        .errorHandler(new PgErrors())
        .build();
  }
  
  
  @Test
  public void getUIOnRoot() {
    final var defaultLocale = RestAssured.when().get("/portal/site");
    defaultLocale.prettyPrint();
    defaultLocale.then().statusCode(200);
  }
  
  public static String getSite() {
    try {
      return IOUtils.toString(SiteExtensionTests.class.getClassLoader().getResource("site.json"), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }


}
