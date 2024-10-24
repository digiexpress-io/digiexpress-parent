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

import org.springframework.security.oauth2.jwt.Jwt;

import io.digiexpress.eveli.client.api.ProcessCommands;

public interface PortalAccessValidator {


  void validateTaskAccess(Long id, Jwt principal) ;

  void validateProcessAccess(ProcessCommands.Process process, Jwt principal);

  void validateProcessIdAccess(String processId, Jwt principal);
  
  void validateProcessAnonymousAccess(String processId, String anonymousUserId);
  
  String getUserName(Jwt principal);
  
  void validateUserAccess(Jwt principal, String userId);
  
}
