package io.digiexpress.eveli.app.version;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/worker/rest/api/version")
public class VersionController {

  @GetMapping
  public ResponseEntity<VersionInfo> getVersionInfo() 
  {
    VersionInfo result = new VersionInfo();
    result.setVersionBuildInfo(VersionProperties.DESCRIBE);
    result.setVersionBuildTimestamp(VersionProperties.BUILD_TIME);
    return ResponseEntity.ok(result);
  }
}
