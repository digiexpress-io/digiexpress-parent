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

import io.digiexpress.eveli.client.api.AssetReleaseCommands.AssetReleaseTag;
import io.digiexpress.eveli.client.api.AssetReleaseCommands.AssetReleaseTagInit;
import io.digiexpress.eveli.client.api.AssetReleaseCommands.ReleaseAssets;
import io.digiexpress.eveli.client.api.AssetTagCommands.AssetTag;
import io.digiexpress.eveli.client.api.ImmutableAssetReleaseTagInit;
import io.digiexpress.eveli.client.api.PortalClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AssetReleaseController {

  private final PortalClient client;

  @GetMapping("/releases/")
  @Transactional
  public ResponseEntity<List<AssetReleaseTag>> getAllReleases() {
    return new ResponseEntity<>(client.assetRelease().findAll(), HttpStatus.OK);
  }
  
  @GetMapping("/releaseTags/wrench")
  @Transactional
  public ResponseEntity<List<AssetTag>> getWrenchTags() {
    return new ResponseEntity<>(client.assetRelease().getWrenchTags(), HttpStatus.OK);
  }

  @GetMapping("/releaseTags/workflow")
  @Transactional
  public ResponseEntity<List<? extends AssetTag>> getWorkflowTags() {
    return new ResponseEntity<>(client.assetRelease().getWorkflowTags(), HttpStatus.OK);
  }

  @GetMapping("/releaseTags/content")
  @Transactional
  public ResponseEntity<List<AssetTag>> getContentTags() {
    return new ResponseEntity<>(client.assetRelease().getContentTags(), HttpStatus.OK);
  }
  
  @PostMapping("/releases/")
  @Transactional
  public ResponseEntity<AssetReleaseTag> create(@RequestBody AssetReleaseTagInit workflowRelease, @AuthenticationPrincipal Jwt principal) {
    String userName = getUserName(principal);
    ImmutableAssetReleaseTagInit tagInit = ImmutableAssetReleaseTagInit.builder().from(workflowRelease).user(userName).build();
    try {
      return new ResponseEntity<>(client.assetRelease().createTag(tagInit), HttpStatus.CREATED);
    }
    catch (org.springframework.dao.DataIntegrityViolationException e) {
      log.warn("Data integrity violation in release creation: {}", e.getMostSpecificCause().getMessage());
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Tag already exists");
    }
  }
  
  @GetMapping("/releases/{name}")
  @Transactional
  public ResponseEntity<AssetReleaseTag> get(@PathVariable("name") String name) {
    final var workflowRelease = client.assetRelease().getByName(name);
    if(workflowRelease.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return new ResponseEntity<>(workflowRelease.get(), HttpStatus.OK);
  }

  @GetMapping("/releaseDownload/{name}")
  @Transactional
  public ResponseEntity<ReleaseAssets> download(@PathVariable("name") String name) {
    final var workflowRelease = client.assetRelease().getAssetRelease(name);
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
