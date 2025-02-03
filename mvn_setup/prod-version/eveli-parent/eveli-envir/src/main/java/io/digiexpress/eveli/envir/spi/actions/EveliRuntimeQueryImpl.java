package io.digiexpress.eveli.envir.spi.actions;

import java.util.Optional;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeploymentStatus;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntimeQuery;
import io.digiexpress.eveli.envir.spi.EveliEnvirStore;
import io.resys.hdes.client.spi.config.HdesClientConfig;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EveliRuntimeQueryImpl implements EveliRuntimeQuery {

  private final EveliEnvirStore ctx;
  private final EveliRuntimeCache cache;
  private final HdesClientConfig hdesClientConfig;
  private boolean isDev;
  

  @Override
  public EveliRuntimeQuery devEnvir(boolean isDevEnvir) {
    this.isDev = isDevEnvir;
    return this;
  }
  
  private EveliRuntime createEnvir(EveliDeployment deployment) {
    final var envir = new EveliRuntimeImpl(deployment, hdesClientConfig, isDev);
    cache.save(envir);
    return envir;
  }
  
  private Uni<EveliRuntime> getOrCreateEnvir(EveliDeployment deployment) {
    final var currentEnvir = cache.get();
    
    // already created
    if(currentEnvir.isPresent() && currentEnvir.get().getDeploymentId().equals(deployment.getId())) {
      return Uni.createFrom().item(currentEnvir.get());
    }
    return new DeploymentQueryImpl(ctx).emptyBranchBody(true)
      .status(EveliDeploymentStatus.DEPLOYED)
      .emptyBranchBody(false)
      .getOneById(deployment.getId())
      .onItem().transform(this::createEnvir);
    
  }
  
  private Uni<Optional<EveliDeployment>> getLastDeployment() {
    return new DeploymentQueryImpl(ctx).emptyBranchBody(true)
      .status(EveliDeploymentStatus.DEPLOYED)
      .emptyBranchBody(true)
      .findAll()
      .onItem().transform(deployments -> deployments.stream()
          .sorted((a, b) -> b.getStartsAt().compareTo(b.getStartsAt()))
          .findFirst()
      );
  }
  
  
  @Override
  public Uni<EveliRuntime> getOne() {
    return getLastDeployment().onItem().transformToUni(last -> {
      if(last.isEmpty()) {
        throw new EveliRuntimeQueryException("No deployments that can be activated!");
      }
      
      return getOrCreateEnvir(last.get());
    });
  }

  @Override
  public Uni<Optional<EveliRuntime>> findOne() {
    return getLastDeployment().onItem().transformToUni(last -> {
      if(last.isEmpty()) {
        return Uni.createFrom().item(Optional.<EveliRuntime>empty());
      }
      
      return getOrCreateEnvir(last.get()).onItem().transform(e -> Optional.of(e));
    });
  }
  
  

  public static class EveliRuntimeQueryException extends RuntimeException {
    private static final long serialVersionUID = -6001308683183662536L;

    public EveliRuntimeQueryException(String error) {
      super(error);
    }

  }
}
