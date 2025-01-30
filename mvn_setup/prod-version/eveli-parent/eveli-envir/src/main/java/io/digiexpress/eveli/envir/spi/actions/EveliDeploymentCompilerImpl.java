package io.digiexpress.eveli.envir.spi.actions;

import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeploymentCompiler;
import io.digiexpress.eveli.envir.spi.EveliEnvirStore;
import io.resys.hdes.client.spi.HdesClientEnvirBuilder;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.resys.hdes.client.spi.config.HdesClientConfig;
import io.resys.hdes.client.spi.envir.ProgramEnvirFactory;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class EveliDeploymentCompilerImpl implements EveliDeploymentCompiler {
  private final EveliEnvirStore ctx;
  private final HdesClientConfig hdesClientConfig;
  private final DialobClient dialobClient;
  
  private String userId;
  private String deploymentId;

  @Override
  public Uni<EveliDeployment> compile() {
    RepoAssert.notEmpty(userId, () -> "userId must be defined!");
    RepoAssert.notEmpty(deploymentId, () -> "deploymentId must be defined!");
    
  
    return null;
  }
  
  
  private void hdes(EveliDeployment deployment) {
    final var builder = new HdesClientEnvirBuilder(new ProgramEnvirFactory(hdesClientConfig), hdesClientConfig.getTypes())
        .tagName(deployment.getName());
    final var envir = ComposerEntityMapper.toEnvir(builder, deployment.getSources().getWrench()).build();     
  }
  
  
  private void syncDialob() {
    dialobClient.getFormTag(userId, deploymentId);
  }
}