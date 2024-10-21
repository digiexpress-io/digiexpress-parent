package io.digiexpress.eveli.client.web.resources;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.digiexpress.eveli.client.api.AssetTagCommands.AssetTagInit;
import io.digiexpress.eveli.client.api.ImmutableAssetTagInit;
import io.digiexpress.eveli.client.api.PortalClient;
import io.digiexpress.eveli.client.api.WorkflowCommands.WorkflowTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WorkflowReleaseController {

  private final PortalClient client;

  @GetMapping("/workflowReleases/")
  @Transactional
  public ResponseEntity<List<WorkflowTag>> getAllWorkflows() {
    return new ResponseEntity<>(client.workflow().release().findAll(), HttpStatus.OK);
  }


  @PostMapping("/workflowReleases/")
  @Transactional
  public ResponseEntity<WorkflowTag> createSnapshot(@RequestBody AssetTagInit workflowRelease, @AuthenticationPrincipal Jwt principal) {
    try {
      String userName = getUserName(principal);
      AssetTagInit snapshotRelease = ImmutableAssetTagInit.builder()
        .name(workflowRelease.getName())
        .description(workflowRelease.getDescription())
        .user(userName)
        .build();
      return new ResponseEntity<>(client.workflow().release().createTag(snapshotRelease), HttpStatus.CREATED);
    }
    catch (org.springframework.dao.DataIntegrityViolationException e) {
      log.warn("Data integrity violation in snapshot release creation: {}", e.getMostSpecificCause().getMessage());
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Tag already exists");
    }
  }
  
  @GetMapping("/workflowReleases/{name}")
  @Transactional
  public ResponseEntity<WorkflowTag> get(@PathVariable("name") String name) {
    final var workflowRelease = client.workflow().release().getByName(name);
    if(workflowRelease.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return new ResponseEntity<>(workflowRelease.get(), HttpStatus.OK);
  }
  
  protected String getUserName(Jwt principal) {
    String userName = "";
    if (principal != null) {
     userName = principal.getClaimAsString("name");
    }
    return userName;
  }
}
