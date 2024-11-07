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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.Sort;

import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.ProcessClient.QueryProcessInstances;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class QueryProcessInstancesImpl implements QueryProcessInstances {
  private final ProcessRepository processJPA;
  
  
  @Override
  public Optional<ProcessClient.ProcessInstance> findOneById(String id) {
    return processJPA.findById(Long.parseLong(id)).map(CreateProcessInstanceImpl::map);
  }
  @Override
  public Optional<ProcessClient.ProcessInstance> findOneByQuestionnaireId(String questionnaireId) {
    return processJPA.findByQuestionnaire(questionnaireId).map(CreateProcessInstanceImpl::map);
  }
  @Override
  public List<ProcessClient.ProcessInstance> findAll() {
    return StreamSupport.stream(processJPA.findAll(Sort.unsorted()).spliterator(), false)
        .map(CreateProcessInstanceImpl::map)
        .collect(Collectors.toList());
  }
  @Override
  public Optional<ProcessClient.ProcessInstance> findOneByTaskId(String id) {
    return processJPA.findByTask(id).map(CreateProcessInstanceImpl::map);
  }
  @Override
  public List<ProcessClient.ProcessInstance> findAllByUserId(String userId) {
    return processJPA.findAllByUserId(userId).stream().map(CreateProcessInstanceImpl::map).toList();
  }

  @Override
  public void deleteOneById(String id) {
    processJPA.deleteById(Long.parseLong(id)); 
  }
}
