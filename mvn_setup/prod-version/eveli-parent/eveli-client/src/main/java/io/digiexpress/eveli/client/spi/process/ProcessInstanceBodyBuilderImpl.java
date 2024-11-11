package io.digiexpress.eveli.client.spi.process;

/*-
 * #%L
 * eveli-client
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

import java.util.Optional;

import io.digiexpress.eveli.client.api.ProcessClient.ProcessInstance;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessInstanceBodyBuilder;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.spi.asserts.ProcessAssert;
import io.digiexpress.eveli.client.spi.asserts.ProcessAssert.ProcessException;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ProcessInstanceBodyBuilderImpl implements ProcessInstanceBodyBuilder {

  private final ProcessRepository processRepository;
  
  private Long processInstanceId;
  private Optional<String> formBody;
  private Optional<String> flowBody;
  
  @Override
  public ProcessInstanceBodyBuilder processInstanceId(Long processInstanceId) {
    ProcessAssert.notNull(processInstanceId, () -> "processInstanceId must be defined!");
    this.processInstanceId = processInstanceId;
    return this;
  }

  @Override
  public ProcessInstanceBodyBuilder formBody(String formBody) {
    this.formBody = Optional.ofNullable(formBody);
    return this;
  }

  @Override
  public ProcessInstanceBodyBuilder flowBody(String flowBody) {
    this.flowBody = Optional.ofNullable(flowBody);
    return this;
  }

  @Override
  public ProcessInstance build() {
    ProcessAssert.notNull(processInstanceId, () -> "processInstanceId must be defined!");
    final var entity = processRepository.findById(processInstanceId).orElseThrow(() -> new ProcessException("Can't find process with id: " + processInstanceId + "!"));
    
    if(formBody != null) {
      entity.setFormBody(formBody.orElse(null));
    }
    if(flowBody != null) {
      entity.setFlowBody(flowBody.orElse(null));
    }    
    
    return CreateProcessInstanceImpl.map(processRepository.save(entity));
  }


}
