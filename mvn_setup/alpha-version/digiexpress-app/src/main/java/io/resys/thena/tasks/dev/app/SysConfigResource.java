package io.resys.thena.tasks.dev.app;

import java.util.List;

import io.resys.sysconfig.client.api.SysConfigClient;
import io.resys.sysconfig.client.api.model.SysConfig;
import io.resys.sysconfig.client.api.model.SysConfigCommand.CreateSysConfig;
import io.resys.sysconfig.client.api.model.SysConfigCommand.SysConfigUpdateCommand;
import io.resys.sysconfig.client.rest.SysConfigRestApi;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
@Singleton
public class SysConfigResource implements SysConfigRestApi {

  @Inject private SysConfigClient sysConfigClient;
  @Inject private CurrentTenant currentTenant;
  @Inject private CurrentUser currentUser;
  @Inject private ProjectClient tenantClient;
  
  @Override
  public Uni<List<SysConfig>> findAllSysConfigs() {
    return getClient().onItem().transformToUni(client -> client.configQuery().findAll());
  }
  @Override
  public Uni<SysConfig> getOneSysConfig(String sysConfigId) {
    return getClient().onItem().transformToUni(client -> client.configQuery().get(sysConfigId));
  }
  @Override
  public Uni<SysConfig> createOneSysConfig(CreateSysConfig commands) {
    return getClient().onItem().transformToUni(client -> client.createConfig().createOne(commands));
  }
  @Override
  public Uni<SysConfig> updateOneSysConfig(String sysConfigId, List<SysConfigUpdateCommand> commands) {
    return getClient().onItem().transformToUni(client -> client.updateConfig().updateOne(commands));
  }
  @Override
  public Uni<SysConfig> deleteOneSysConfig(String sysConfigId, List<SysConfigUpdateCommand> command) {
    return getClient().onItem().transformToUni(client -> client.updateConfig().updateOne(command));
  }
  private Uni<SysConfigClient> getClient() {
    return sysConfigClient.withTenantConfig(currentTenant.getTenantId());
  }
}
