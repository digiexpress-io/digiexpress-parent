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

import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.api.ProcessClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PortalAccessValidatorImpl implements PortalAccessValidator {

  private final ProcessClient processClient;

  @Override
  public void validateTaskAccess(Long id, CrmClient.CustomerPrincipal principal) {
    if (id == null || principal == null) {
      log.error("Access violation by user: {} to access task by id: {}", principal.getUsername(), id);
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Access violation");
    }
    final var process = getProcessFromTask(id.toString());
    if (process == null) {
      log.error("Access violation by user: {}, process by task id {} not found", principal.getUsername(), id);
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Access violation");
    }    
    validateProcessAccess(process, principal);
  }

  @Override
  public void validateProcessAccess(ProcessClient.ProcessInstance process, CrmClient.CustomerPrincipal principal) {
    if (process == null) {
      log.error("Access violation by user: {}, process not found", principal.getUsername());
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Access violation");
    } 
    final var userId = process.getUserId();
    validateUserAccess(principal, userId);
  }

  @Override
  public void validateProcessIdAccess(String processId, CrmClient.CustomerPrincipal principal) {
    final var process = processClient.queryInstances().findOneById(processId).orElse(null);
    if (process == null) {
      log.error("Access violation by user: {}, process by id {} not found", principal.getUsername(), processId);
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Access violation");
    } 
    validateProcessAccess(process, principal);
  }
  

  protected ProcessClient.ProcessInstance getProcessFromTask(String taskId) {
    return processClient.queryInstances().findOneByTaskId(Long.parseLong(taskId)).orElse(null);
  }
  
  @Override
  public void validateUserAccess(CrmClient.CustomerPrincipal principal, String userId) {
    if (principal == null) {
      log.error("Access violation, missing principal");
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Access violation");
    }
    
    
    if (!StringUtils.equals(Optional.ofNullable(principal).map(p -> p.getUsername()).orElse(null), userId) &&
        !StringUtils.equals(Optional.ofNullable(principal.getRepresentedId()).orElse(null), userId)) {
      log.error("Access violation by user: {}, unmatched user ID: {}", principal.getUsername(), userId);
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Access violation");
    }
  }


  @Override
  public void validateProcessAnonymousAccess(String processId, String anonymousUserId) {
    final var process = processClient.queryInstances().findOneById(processId).orElse(null);
    if (!anonymousUserId.equals(process.getUserId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access violation, not anonymous process");
    }
  }
}
