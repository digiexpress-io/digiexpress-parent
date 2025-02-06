package io.digiexpress.eveli.client.web.resources.assets;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RestController
@RequiredArgsConstructor
@RequestMapping("/worker/rest/api/assets/deployments")
@Slf4j
public class AssetsDeploymentController {
  
  private final EveliEnvirClient composer;
  
  @GetMapping("/{name}")
  public Uni<EveliDeployment> download(@PathVariable("name") String name) {
    return composer.deploymentQuery().emptyBranchBody(false).getOneById(name);
  }

}
