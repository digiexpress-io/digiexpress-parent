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

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.digiexpress.eveli.client.api.ProcessClient.PaginateProcessInstances;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessInstance;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessStatus;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.spi.asserts.ProcessAssert;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j 
@Data 
@Accessors(fluent=true)
public class PaginateProcessInstancesImpl implements PaginateProcessInstances {
  private final ProcessRepository processJPA;
  
  private String name;
  private String userId;
  private List<String> status;

  private Pageable page;

  @Override
  public Page<ProcessInstance> findAll() {
    ProcessAssert.notNull(name, () -> "name must be defined!");
    ProcessAssert.notNull(userId, () -> "userId must be defined!");
    ProcessAssert.notNull(page, () -> "page must be defined!");
    
    List<ProcessStatus> statusList = new ArrayList<>();
    if (status == null || status.isEmpty()) {
      status = new ArrayList<>();
      for (final var  val : ProcessStatus.values()) {
        statusList.add(val);
      }
    } else {
      for (String s : status) {
        try {
          var statusEnum = ProcessStatus.valueOf(s);
          statusList.add(statusEnum);
        } catch (IllegalArgumentException | NullPointerException e) {
          // ignore
          log.warn("Incorrect process status to find method: {}", s);
        }
      }
    }
    
    return processJPA.searchProcesses('%' + name + '%', statusList, userId, page).map(CreateProcessInstanceImpl::map);
  }
}
