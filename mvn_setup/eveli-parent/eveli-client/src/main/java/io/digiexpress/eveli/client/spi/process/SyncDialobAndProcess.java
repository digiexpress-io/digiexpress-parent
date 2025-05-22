package io.digiexpress.eveli.client.spi.process;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.api.questionnaire.Questionnaire.Metadata.Status;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessInstance;
import io.digiexpress.eveli.dialob.api.DialobClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class SyncDialobAndProcess {

  private final ProcessClient processClient;
  private final DialobClient dialobClient;
  private final ObjectMapper objectMapper;
  
  
  @Transactional
  public void executeFlowForInstance(ProcessInstance init) {
    // resync
  
    try {

      Questionnaire questionnaire = null;
      try {
        questionnaire = dialobClient.getQuestionnaireAndMetaById(init.getQuestionnaireId());
        if(questionnaire.getMetadata().getStatus() != Status.COMPLETED) {
          log.debug("Skipping execution because questionnaire: {} state is not completed!", init.getQuestionnaireId());
          return;
        }
      } catch(Exception e) { }
      
      if(questionnaire == null) {
        log.error("Skipping execution because questionnaire: {} could not be found!", init.getQuestionnaireId());
        return;
      }
      
      
      
      final var optional = processClient.queryInstances().findOneByIdAndLock(init.getId().toString());
      if(optional.isEmpty()) {
        log.debug("Skipping execution: {} because task is already created, process status handling is probably wrong!", init.getId());
        return;          
      }
      
      final var instance = optional.get();
      if(instance.getTaskId() != null) {
        log.debug("Skipping execution: {} because task is already created, process status handling is probably wrong!", instance.getId());
        return;
      }
      
      processClient.createBodyBuilder()
        .processInstanceId(instance.getId())
        .formBody(objectMapper.writeValueAsString(questionnaire))
        .build();
      
      final var flow = processClient.createExecutor().processInstance(instance).execute();
      
      processClient.createBodyBuilder()
        .processInstanceId(instance.getId())
        .flowBody(objectMapper.writeValueAsString(flow))
        .build();
      
    } catch(Exception e) {
      log.error("Failed to run flow for process instance: {}, e: {}!", init.getId(), e.getMessage(), e);
    }
  }
}
