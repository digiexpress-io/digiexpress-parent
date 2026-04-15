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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.limaone.model.ImmutableLocaleLabel;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.tests.support.DbSupport;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Authoring_2_Test extends DbSupport {

  @Test
  public void tagomiTestWith2LocalesAndOnePrinoutAssertingServiceName() {
    final var authoring = new AuthoringImpl(createConfig());   
   
    final var locale1 = authoring.newModel()
        .newLocale()
        .props(props -> props.locale("en"))
        .buildSync();
    
    final var locale2 = authoring.newModel()
        .newLocale()
        .props(props -> props.locale("fi"))
        .buildSync();
    

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder
            .serviceName("application for loan")
            .addLabels(ImmutableLocaleLabel.builder().locale(locale1.getId()).labelValue("English printout name").build())
            .orchestratorName("wrench flow 1, we dont have it yet")
            .build()
        )
        .buildSync();
    Assertions.assertNotNull(printout1.getId());

    {
      final var worldState = authoring.worldQuery().docs(BodyType.values()).findAllSync();
      Assertions.assertEquals(1, worldState.getPrintouts().size()); 
      
      final var body = worldState.getPrintouts().values().iterator().next().getBody();
      Assertions.assertEquals("application for loan",  body.getServiceName()); 
    }
    
    authoring.newModel()
      .newDeployment()
      .props(props -> props.name("v1.5").description("test release"))
      .buildSync();
     
  }
}
