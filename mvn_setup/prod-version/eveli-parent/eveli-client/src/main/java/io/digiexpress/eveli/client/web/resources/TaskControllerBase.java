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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import io.digiexpress.eveli.client.persistence.entities.TaskAccessEntity;
import io.digiexpress.eveli.client.persistence.entities.TaskAccessId;
import io.digiexpress.eveli.client.persistence.entities.TaskEntity;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskControllerBase {

  private final TaskAccessRepository taskAccessRepository;
  
  public TaskControllerBase(TaskAccessRepository taskAccessRepository) {
    this.taskAccessRepository = taskAccessRepository;
  }
  
  protected String userName(Authentication authentication) {
    return Optional.ofNullable(authentication).map(auth->auth.getName()).orElse("UNAUTHENTICATED");
  }
  
  protected String getUserName(Jwt principal) {
    String userName = "";
    if (principal != null) {
     userName = principal.getClaimAsString("name");
    }
    return userName;
  }

  protected String getEmail(Jwt principal) {
    String email = "";
    if (principal != null) {
      email = principal.getClaimAsString("email");
    }
    return email;
  }

  protected void registerTaskAccess(Object id, Authentication authentication, Optional<TaskEntity> result) {

    String userName = "ANONYMOUS";
    if (authentication != null && authentication.getName() != null) {
      userName = authentication.getName();
    }
    registerUserTaskAccess(id, result, userName);
  }

  protected void registerUserTaskAccess(Object id, Optional<TaskEntity> result, String userName) {
    if (result.isPresent()) {
      log.info("Registering access to task with id: {} by user {}", id, userName);
      try {
        TaskAccessId accessId = new TaskAccessId(result.get(), userName);
        TaskAccessEntity access = taskAccessRepository.findById(accessId).orElse(new TaskAccessEntity(accessId));
        access.setUpdated(ZonedDateTime.now(ZoneId.of("UTC")));
        taskAccessRepository.save(access);
      }
      catch (Exception e) {
        // TODO: called twice for external and internal comments, could result in duplicate key error
        // ignore such errors
        log.warn("Task access error: {}", e.getMessage());
      }
    }
    else {
      log.warn("No task for id {}, registration skipped", id);
    }
  }
 
}
