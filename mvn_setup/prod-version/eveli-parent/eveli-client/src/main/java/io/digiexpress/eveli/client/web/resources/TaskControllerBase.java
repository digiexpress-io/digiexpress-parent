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

import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.persistence.entities.TaskAccessEntity;
import io.digiexpress.eveli.client.persistence.entities.TaskAccessId;
import io.digiexpress.eveli.client.persistence.entities.TaskEntity;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TaskControllerBase {

  private final TaskAccessRepository taskAccessRepository;
  
  
  protected String userName(Authentication authentication) {
    return Optional.ofNullable(authentication).map(auth->auth.getName()).orElse("UNAUTHENTICATED");
  }
  

  protected void registerTaskAccess(Object id, AuthClient.UserPrincipal authentication, Optional<TaskEntity> result) {
    registerUserTaskAccess(id, result, authentication.getUsername());
  }

  public void registerUserTaskAccess(Object id, Optional<TaskEntity> result, String userName) {
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
