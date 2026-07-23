package io.resys.limaone.tests;

import java.util.List;

import org.junit.jupiter.api.Assertions;

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

import org.junit.jupiter.api.Test;

import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.tests.support.DbSupport;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Authoring_9_Copy_Test extends DbSupport {

  
  @Test
  public void stencilAssetTest() {
    final var authoring = new AuthoringImpl(createConfig());
    
    
    final var template1 = authoring.newModel()
        .newArticleTemplate()
        .props(props -> props.name("Template1").content("# Header 1").type("Page"))
        .buildSync();
    
   final var copiedTemplate = authoring.copyAsModel().copyAny()
       .props(props -> props.idOfObjectToCopy(template1.getId())
           .newObjectName("copied template 1"))
       .buildSync();
    
    {
      final var fs = authoring.worldFsQuery().findAllSync().flatAll();
      final var actual = fs.stream().filter(e -> e.getId().equals(copiedTemplate.getId())).findFirst();
      log.debug("copied: {}", actual);
      
      Assertions.assertEquals(true, actual.isPresent());
    }
        
    final var flow1 = authoring.newModel()
        .newFlow()
        .props(builder -> builder.name("Flow1"))
        .buildSync();
    
    final var copiedFlow1 = authoring.copyAsModel().copyAny().props(props -> props.idOfObjectToCopy(flow1.getId()).newObjectName("copied flow 1")).buildSync();
    
    {
      final var fs = authoring.worldFsQuery().findAllSync().flatAll();
      final var actual = fs.stream().filter(e -> e.getId().equals(copiedFlow1.getId())).findFirst();
      log.debug("copied: {}", actual);
      
      Assertions.assertEquals(true, actual.isPresent());
    }
    
    final var flowTask1 = authoring.newModel()
        .newFlowTask()
        .props(builder -> builder.name("FlowTask1"))
        .buildSync();
    
    final var copiedFlowTask1 = authoring.copyAsModel().copyAny()
        .props(props -> props.idOfObjectToCopy(flowTask1.getId())
            .newObjectName("copied_flowTask_1"))
        .buildSync();
    
    {
      final var fs = authoring.worldFsQuery().findAllSync().flatAll();
      final var actual = fs.stream().filter(e -> e.getId().equals(copiedFlowTask1.getId())).findFirst();
      log.debug("copied: {}", actual);
      
      Assertions.assertEquals(true, actual.isPresent());
    }
 
    final var decisionTable1 = authoring.newModel()
        .newDecisionTable()
        .props(builder -> builder.name("DecisionTable1"))
        .buildSync();
    
    final var copiedDecisionTable1 = authoring.copyAsModel().copyAny()
        .props(props -> props.idOfObjectToCopy(decisionTable1.getId())
            .newObjectName("copied decisionTable 1"))
        .buildSync();
    
    {
      final var fs = authoring.worldFsQuery().findAllSync().flatAll();
      final var actual = fs.stream().filter(e -> e.getId().equals(copiedDecisionTable1.getId())).findFirst();
      log.debug("copied: {}", actual);

      Assertions.assertEquals(true, actual.isPresent());
    }

    final var printout1 = authoring.newModel()
        .newPrintout()
        .props(builder -> builder.serviceName("Printout1").orchestratorName("flow-1"))
        .buildSync();

    final var copiedPrintout1 = authoring.copyAsModel().copyAny()
        .props(props -> props.idOfObjectToCopy(printout1.getId())
            .newObjectName("copied printout 1")).buildSync();

    {
      final var fs = authoring.worldFsQuery().findAllSync().flatAll();
      final var actual = fs.stream().filter(e -> e.getId().equals(copiedPrintout1.getId())).findFirst();
      log.debug("copied: {}", actual);

      Assertions.assertEquals(true, actual.isPresent());
    }

    final var printoutResource1 = authoring.newModel()
        .newPrintoutResource()
        .props(builder -> builder.resourceName("header.typst").contentType("text/*").uploadBody("some text for printout").printoutPageIds(List.of()))
        .buildSync();

    final var copiedPrintoutResource1 = authoring.copyAsModel().copyAny()
        .props(props -> props.idOfObjectToCopy(printoutResource1.getId())
            .newObjectName("copied printout resource 1"))
        .buildSync();

    {
      final var fs = authoring.worldFsQuery().findAllSync().flatAll();
      final var actual = fs.stream().filter(e -> e.getId().equals(copiedPrintoutResource1.getId())).findFirst();
      log.debug("copied: {}", actual);

      Assertions.assertEquals(true, actual.isPresent());
    }
    
  
  }
}
