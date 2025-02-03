package io.digiexpress.eveli.envir.spi.actions;

import java.time.OffsetDateTime;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;
import io.resys.hdes.client.api.HdesClient.ExecutorBuilder;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.spi.HdesClientEnvirBuilder;
import io.resys.hdes.client.spi.HdesClientExecutorBuilder;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.resys.hdes.client.spi.config.HdesClientConfig;
import io.resys.hdes.client.spi.envir.ProgramEnvirFactory;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.api.StencilEnvir;
import io.thestencil.client.spi.builders.StencilEnvirImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class EveliRuntimeImpl implements EveliRuntime {
  private final HdesClientConfig hdesClientConfig;
  private final ProgramEnvir wrenchEnvir;
  private final StencilEnvir stencilEnvir;
  
  public final String name;
  public final String deploymentId;
  private final boolean isDev;
  
  public EveliRuntimeImpl(
      EveliDeployment deployment, 
      HdesClientConfig hdesClientConfig,
      boolean isDev) {
    
    this.hdesClientConfig = hdesClientConfig;
    this.wrenchEnvir = new HdesClientEnvirBuilder(new ProgramEnvirFactory(hdesClientConfig), hdesClientConfig.getTypes())
        .tagName(deployment.getName())
        .callback(builder -> ComposerEntityMapper.toEnvir(builder, deployment.getSources().getWrench()).build())
        .build();
    
    this.stencilEnvir = StencilEnvirImpl.of(deployment.getSources().getStencil(), deployment.getName(), isDev);
    this.name = deployment.getName();
    this.deploymentId = deployment.getId();
    this.isDev = isDev;
  }
  
  
  @Override
  public String getName() {
    return name;
  }
  @Override
  public String getDeploymentId() {
    return deploymentId;
  }
  @Override
  public ExecutorBuilder getWrench() {
    return new HdesClientExecutorBuilder(wrenchEnvir, hdesClientConfig.getTypes(), hdesClientConfig.getDependencyInjectionContext());
  }
  @Override
  public Sites getStencil(OffsetDateTime now) {
    return stencilEnvir.get(now);
  }
  public boolean isDev() {
    return isDev;
  }
}
