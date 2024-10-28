package io.digiexpress.eveli.client.iam;

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

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.PortalClient;
import io.digiexpress.eveli.client.api.ProcessCommands;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PortalAccessValidatorImpl implements PortalAccessValidator {

  private final PortalClient client;
  
  public PortalAccessValidatorImpl(PortalClient client) {
    this.client = client;
  }

  public void validateTaskAccess(Long id, AuthClient.Principal principal) {
    if (id == null || principal == null) {
      log.error("Access violation by user: {} to access task by id: {}", getUserName(principal), id);
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Access violation");
    }
    final var process = getProcessFromTask(id.toString());
    if (process == null) {
      log.error("Access violation by user: {}, process by task id {} not found", getUserName(principal), id);
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Access violation");
    }    
    validateProcessAccess(process, principal);
  }

  public void validateProcessAccess(ProcessCommands.Process process, AuthClient.Principal principal) {
    if (process == null) {
      log.error("Access violation by user: {}, process not found", getUserName(principal));
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Access violation");
    } 
    final var userId = process.getUserId();
    validateUserAccess(principal, userId);
  }

  public void validateProcessIdAccess(String processId, AuthClient.Principal principal) {
    final var process = client.process().query().get(processId).orElse(null);
    if (process == null) {
      log.error("Access violation by user: {}, process by id {} not found", getUserName(principal), processId);
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Access violation");
    } 
    validateProcessAccess(process, principal);
  }
  
  public String getUserName(AuthClient.Principal principal) {
    return principal.getUserName();
  }
  
  protected ProcessCommands.Process getProcessFromTask(String taskId) {
    return client.process().query().getByTaskId(taskId).orElse(null);
  }
  
  public void validateUserAccess(AuthClient.Principal principal, String userId) {
    if (principal == null) {
      log.error("Access violation, missing principal");
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Access violation");
    }
    if (!StringUtils.equals(Optional.ofNullable(principal).map(p->p.getUserName()).orElse(null), userId) &&
        !StringUtils.equals(Optional.ofNullable(principal.getRepresentedId()).orElse(null), userId)) {
      log.error("Access violation by user: {}, unmatched user ID: {}", getUserName(principal), userId);
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Access violation");
    }
  }


  @Override
  public void validateProcessAnonymousAccess(String processId, String anonymousUserId) {
    final var process = client.process().query().get(processId).orElse(null);
    if (!anonymousUserId.equals(process.getUserId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access violation, not anonymous process");
    }
  }
}
