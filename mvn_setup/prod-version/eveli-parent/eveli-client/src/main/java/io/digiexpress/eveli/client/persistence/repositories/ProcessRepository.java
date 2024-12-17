package io.digiexpress.eveli.client.persistence.repositories;

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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import io.digiexpress.eveli.client.api.ProcessClient.ProcessStatus;
import io.digiexpress.eveli.client.persistence.entities.ProcessEntity;

public interface ProcessRepository extends PagingAndSortingRepository<ProcessEntity, Long>{
  Optional<ProcessEntity> findByQuestionnaireId(String questionnaireId);
  Optional<ProcessEntity> findByTaskId(String taskId);
  Optional<ProcessEntity> findById(Long id);
  
  @Query(value=
      "select p from ProcessEntity p where " +
      " lower(workflowName) like :name" +
      " and (:userId='' or userId = :userId)" +
      " and status in :status")
  Page<ProcessEntity> searchProcesses(
      @Param("name") String name,
      @Param("status") List<ProcessStatus> status,
      @Param("userId") String userId,
      Pageable page);

  @Query(value="select p from ProcessEntity p where userId = :userId")
  List<ProcessEntity> findAllByUserId(@Param("userId") String userId);

  @Query(value="select p from ProcessEntity p where status = :status and taskId is null")
  List<ProcessEntity> findAllByStatus(ProcessStatus status);
  

  @Query(nativeQuery = true, value=
"""
SELECT * FROM process 
WHERE expires_in_seconds is not null
and created + make_interval(secs => expires_in_seconds) < expires_at
and status in('CREATED', 'ANSWERING')
""")
  List<ProcessEntity> findAllByExpiration();
  
  @Query(nativeQuery = true, value=
"""
SELECT form_body FROM process 
WHERE task_id = :taskId and form_body is not null
""")
  Optional<String> findQuestionnaireByTaskId(String taskId);
  
  void deleteById(@Param("id") Long id);
  ProcessEntity save(ProcessEntity entity);
}
