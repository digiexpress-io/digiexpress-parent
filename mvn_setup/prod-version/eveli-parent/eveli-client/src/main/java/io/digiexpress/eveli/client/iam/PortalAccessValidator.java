package io.digiexpress.eveli.client.iam;

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
