package io.digiexpress.eveli.client.iam;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import io.digiexpress.eveli.client.api.PortalClient;
import io.digiexpress.eveli.client.api.ProcessCommands;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PortalAccessValidatorImpl implements PortalAccessValidator {

  private final PortalClient client;
  
  public PortalAccessValidatorImpl(PortalClient client) {
    this.client = client;
  }

  public void validateTaskAccess(Long id, Jwt principal) {
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

  public void validateProcessAccess(ProcessCommands.Process process, Jwt principal) {
    if (process == null) {
      log.error("Access violation by user: {}, process not found", getUserName(principal));
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Access violation");
    } 
    final var userId = process.getUserId();
    validateUserAccess(principal, userId);
  }

  public void validateProcessIdAccess(String processId, Jwt principal) {
    final var process = client.process().query().get(processId).orElse(null);
    if (process == null) {
      log.error("Access violation by user: {}, process by id {} not found", getUserName(principal), processId);
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Access violation");
    } 
    validateProcessAccess(process, principal);
  }
  
  public String getUserName(Jwt principal) {
    String userName = "UNAUTHENTICATED";
    if (principal != null) {
     userName = principal.getClaimAsString("name");
    }
    return userName;
  }
  
  protected ProcessCommands.Process getProcessFromTask(String taskId) {
    return client.process().query().getByTaskId(taskId).orElse(null);
  }
  
  public void validateUserAccess(Jwt principal, String userId) {
    if (principal == null) {
      log.error("Access violation, missing principal");
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Access violation");
    }
    if (!StringUtils.equals(Optional.ofNullable(principal).map(p->p.getSubject()).orElse(null), userId) &&
        !StringUtils.equals(Optional.ofNullable(getRepresentedId(principal)).orElse(null), userId)) {
      log.error("Access violation by user: {}, unmatched user ID: {}", getUserName(principal), userId);
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Access violation");
    }
  }
  
  @SuppressWarnings({"unchecked"})
  private String getRepresentedId(Jwt principal) {
    Map<String, Object> map = (Map<String, Object>)Optional.ofNullable(principal).map(p->p.getClaims()).map(c->c.get("representedPerson")).orElse(null);
    if (map != null) {
      Object value = map.get("personId");
      if (value != null) {
        return value.toString();
      }
    }
    else {
      map = (Map<String, Object>)Optional.ofNullable(principal).map(p->p.getClaims()).map(c->c.get("representedOrganization")).orElse(null);
      if (map != null) {
        Object value = map.get("identifier");
        if (value != null) {
          return value.toString();
        }
      }
    }
    return null;
  }

  @Override
  public void validateProcessAnonymousAccess(String processId, String anonymousUserId) {
    final var process = client.process().query().get(processId).orElse(null);
    if (!anonymousUserId.equals(process.getUserId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access violation, not anonymous process");
    }
  }
}
