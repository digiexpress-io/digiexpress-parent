package io.resys.thena.grim.test;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import io.resys.thena.api.actions.TenantActions.TenantOperationStatus;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessStatus;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessType;
import io.resys.thena.grim.test.config.DbTestTemplate;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class SimpleGrimProcTest extends DbTestTemplate {

  @Test
  public void createAndUpdateProc() {
    // create project
    CreatedTenant repo = getClient().tenants().createOneTenant()                      
        .name("SimpleGrimProcTest-1", StructureType.grim)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(TenantOperationStatus.OK, repo.getStatus());
    
    
    getClient().grim(repo).commit().createOneProc()
      .commitAuthor("john.doe@morgue.com")
      .commitMessage("viewed")
      
      .proc(newProc -> {
        
        newProc
        .expiresAt(null)
        .expiresInSeconds(null)
        .expiresAt(null)
        .status(GrimProcessStatus.ANSWERED)
        
        .questionnaireId("questionnaireId")
        .userId("userId")
        
        .workflowName("workflowName")
        .articleName("articleName")
        .parentArticleName("parentArticleName")
        .anon(false)
        .formName("formName")
        .flowName("flowName")
        .missionId(null)
        .cockpitId(null)
        
        .formTagName("formTagName")
        .stencilTagName("stencilTagName")
        .wrenchTagName("wrenchTagName")
        .type(GrimProcessType.CUSTOMER_ASSIGNMENT)
        .build();
      })
      .build()
      .await().atMost(Duration.ofMinutes(1));
    
  }
}
