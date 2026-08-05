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
  public void propertyCreationTest() {
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
      final var worldState = authoring.worldQuery().docs(BodyType.PROPERTY_OBJECT).findAllSync();
      Assertions.assertEquals(2, worldState.getPropertyObjects().size()); 
      
      final var body = worldState.getPropertyObjects().values().iterator().next().getBody();
      Assertions.assertEquals("default",  body.getObjectType()); 
      Assertions.assertEquals("first",  worldState.getPropertyObjects().get(property1.getId()).getBody().getName()); 
    }
    
    authoring.newModel()
      .newDeployment()
      .props(props -> props.name("po1.0").description("test release"))
      .buildSync();
     
  }
  
  @Test
  public void propertyDuplicateRejectTest() {
    final var authoring = new AuthoringImpl(createConfig());   
   
    final var property1 = authoring.newModel()
        .newPropertyObject()
        .props(builder -> builder.objectType("default").name("first").content("{\"firstObjectValue\":\"one\"}").build())
        .buildSync();
    Assertions.assertNotNull(property1.getId());
    Assertions.assertThrows(Exception.class, () -> authoring.newModel()
          .newPropertyObject()
          .props(builder -> builder.objectType("default").name("first").content("{\"firstObjectValue\":\"two\"}").build())
          .buildSync());
    final var worldState = authoring.worldQuery().docs(BodyType.values()).findAllSync();
    Assertions.assertEquals(1, worldState.getPropertyObjects().size()); 
    
    final var body = worldState.getPropertyObjects().values().iterator().next().getBody();
    Assertions.assertEquals("default",  body.getObjectType()); 
    Assertions.assertEquals("first",  body.getName()); 
    
    authoring.newModel()
      .newDeployment()
      .props(props -> props.name("po1.0").description("test release"))
      .buildSync();
     
  }
  @Test
  public void propertyModificationTest() {
    final var authoring = new AuthoringImpl(createConfig());   
    
    final var property1 = authoring.newModel()
        .newPropertyObject()
        .props(builder -> builder.objectType("default").name("first").content("{\"firstObjectValue\":\"one\"}").build())
        .buildSync();
    Assertions.assertNotNull(property1.getId());

    final var worldState = authoring.worldQuery().docs(BodyType.values()).findAllSync();
    Assertions.assertEquals(1, worldState.getPropertyObjects().size()); 
    
    final var body = worldState.getPropertyObjects().values().iterator().next().getBody();
    Assertions.assertEquals("default",  body.getObjectType()); 
    Assertions.assertEquals("first",  body.getName()); 

    final var updated = authoring.modifyModel()
        .modifyPropertyObject()
        .props(props -> props
            .propertyObjectId(property1.getId())
            .content("{\"firstObjectValue\":\"one_plus_one\"}")
            .build()
        )
        .buildSync();

    final var updatedWorldState = authoring.worldQuery().docs(BodyType.values()).findAllSync();
    final var updatedBody = updatedWorldState.getPropertyObjects().values().iterator().next().getBody();
    Assertions.assertEquals("default",  updatedBody.getObjectType()); 
    Assertions.assertEquals("first",  updatedBody.getName()); 
    Assertions.assertEquals("{\"firstObjectValue\":\"one_plus_one\"}",  updatedBody.getContent()); 
    
    
  }
}
