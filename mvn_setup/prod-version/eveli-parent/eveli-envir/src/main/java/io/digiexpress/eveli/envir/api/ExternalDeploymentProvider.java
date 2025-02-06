package io.digiexpress.eveli.envir.api;

import java.util.Optional;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.smallrye.mutiny.Uni;

public interface ExternalDeploymentProvider {

  Uni<Optional<EveliDeployment>> getDeployment();
  
}
