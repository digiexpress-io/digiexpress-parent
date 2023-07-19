package io.thestencil.quarkus.iam.deployment.test;

/*-
 * #%L
 * quarkus-stencil-iam-deployment
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;


//-Djava.util.logging.manager=org.jboss.logmanager.LogManager
public class IAMDeploymentTest {
  @RegisterExtension
  final static QuarkusUnitTest config = new QuarkusUnitTest()
    .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
      .addAsResource(new StringAsset(
          "quarkus.user-iam.person-security-proxy.host=test\r\n"+ 
          "quarkus.user-iam.person-security-proxy.path=test\r\n" +
              
          "quarkus.user-iam.company-security-proxy.host=test\r\n"+ 
          "quarkus.user-iam.company-security-proxy.path=test\r\n" 
          ), "application.properties")
    );
  
  @Test
  public void getUserActions() {
    final var response = RestAssured.when().get("portal-app/iam");

    response.then().assertThat().statusCode(200);
    
    final var body = response.asString();
    Assertions.assertEquals("{\"type\":\"ANONYMOUS\",\"user\":null}", body);
  }
  
}
