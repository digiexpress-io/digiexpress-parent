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

import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.tests.support.DbSupport;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class PropertyObject_Test extends DbSupport {

  @Test
  public void propertyTest1() {
    final var authoring = new AuthoringImpl(createConfig());   
   
    final var property1 = authoring.newModel()
        .newPropertyObject()
        .props(builder -> builder.objectType("default").name("first").content("{\"firstObjectValue\":\"one\"}").build())
        .buildSync();
    final var property2 = authoring.newModel()
        .newPropertyObject()
        .props(builder -> builder.objectType("default").name("second").content("{\"firstObjectValue\":\"two\"}").build())
        .buildSync();

    Assertions.assertNotNull(property1.getId());
    Assertions.assertNotNull(property2.getId());

    {
      final var worldState = authoring.worldQuery().docs(BodyType.values()).findAllSync();
      Assertions.assertEquals(2, worldState.getPropertyObjects().size()); 
      
      final var body = worldState.getPropertyObjects().values().iterator().next().getBody();
      Assertions.assertEquals("default",  body.getObjectType()); 
    }
    
    authoring.newModel()
      .newDeployment()
      .props(props -> props.name("po1.0").description("test release"))
      .buildSync();
     
  }
}
