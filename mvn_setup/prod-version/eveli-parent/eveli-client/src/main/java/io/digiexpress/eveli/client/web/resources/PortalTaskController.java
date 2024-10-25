package io.digiexpress.eveli.client.web.resources;

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
import java.util.Collection;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.PortalClient;
import io.digiexpress.eveli.client.api.TaskCommands;
import io.digiexpress.eveli.client.iam.PortalAccessValidator;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import io.digiexpress.eveli.client.spi.TaskCommandsImpl;
import lombok.extern.slf4j.Slf4j;

@RestController
@Transactional
@Slf4j
/*
 * Task controller for portal backend access.
 */
public class PortalTaskController extends TaskControllerBase
{
    private final TaskRepository taskRepository;
    private final PortalAccessValidator accessValidator;
    

    public PortalTaskController(
        TaskAccessRepository taskAccessRepository, 
        TaskRepository taskRepository, 
        PortalClient client,
        PortalAccessValidator validator)
    {
      super(taskAccessRepository);
      this.taskRepository = taskRepository;
      this.accessValidator = validator;
    }

    @GetMapping("/task/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<TaskCommands.Task> getTaskById(@PathVariable("id") Long id, @AuthenticationPrincipal Jwt principal) 
    {
      log.debug("Task portal get by user {} for task id {}", getUserName(principal), id);
      accessValidator.validateTaskAccess(id, principal);
      final var result = taskRepository.findById(id);
      return result
          .map(TaskCommandsImpl::map) 
          .map(ResponseEntity::ok) 
          .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value="/externalTasksUnread")
    @Transactional(readOnly = true)
    public ResponseEntity<Collection<Long>> getUnreadExternalTasks(@RequestParam("userId") String userId, 
        @AuthenticationPrincipal Jwt principal) 
    {
      log.debug("Task portal unread request by user id: {}", getUserName(principal));
      accessValidator.validateUserAccess(principal, userId);
      List<Long> taskIds = new ArrayList<>();
      Iterable<Long> accesses = taskRepository.findUnreadExternalTasks(userId);
      accesses.forEach(access->taskIds.add(access));
      return new ResponseEntity<>(taskIds,HttpStatus.OK);
    }
}
