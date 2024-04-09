package io.resys.thena.tasks.dev.app;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

import io.dialob.client.spi.DialobComposerImpl;
import io.resys.crm.client.api.CrmClient;
import io.resys.crm.client.api.model.Customer;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.sysconfig.client.api.SysConfigClient;
import io.resys.sysconfig.client.mig.MigrationClient;
import io.resys.sysconfig.client.mig.model.MigrationAssets;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.dev.app.demo.RandomDataProvider;
import io.resys.thena.tasks.dev.app.demo.TaskGen;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.spi.StencilComposerImpl;
import io.vertx.mutiny.core.Vertx;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("q/demo/api/")
public class DemoResource {
  @Inject Vertx vertx;
  @Inject TaskClient taskClient;
  @Inject CrmClient crmClient;
  @Inject TenantConfigClient tenantClient;
  @Inject CurrentTenant currentTenant;
  @Inject SysConfigClient sysConfigClient;
  

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("populate/tasks/{totalTasks}")
  public Uni<TenantConfig> populate(@PathParam("totalTasks") String totalTasks) {
    final var provider =  new RandomDataProvider();
    final var windows = provider.windows(totalTasks == null ? 50 : Integer.parseInt(totalTasks));

    return tenantClient.queryActiveTenantConfig().get(currentTenant.tenantId())
    .onItem().transformToUni(config -> {
      final var crmConfig = config.getRepoConfigs().stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.CRM).findFirst().get();
      final var taskConfig = config.getRepoConfigs().stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.TASKS).findFirst().get();

      return crmClient.withRepoId(crmConfig.getRepoId()).createCustomer().upsertMany(new TaskGen().generateCustomers(windows))
          .onItem().transformToUni((List<Customer> data) -> {
            final var bulkTasks = new TaskGen().generateTasks(windows, data);
            return taskClient.withRepoId(taskConfig.getRepoId()).tasks().createTask().createMany(bulkTasks);
          }).onItem().transform(junk -> config);
    });
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("clear/tasks")
  public Uni<TenantConfig> clear() {
    return tenantClient.queryActiveTenantConfig().get(currentTenant.tenantId())
    .onItem().transformToUni(config -> {
        final var taskConfig = config.getRepoConfigs().stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.TASKS).findFirst().get();
        return taskClient.withRepoId(taskConfig.getRepoId()).tasks().queryActiveTasks().deleteAll("", Instant.now())
            .onItem().transform(tasks -> config);
    });
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("reinit")
  public Uni<Void> reinit() {
    return tenantClient.query().deleteAll().onItem().transformToUni(junk -> Uni.createFrom().voidItem());
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("reinit-assets")
  public Uni<SiteState> reinitAssets() {
    final var mig = new MigrationClient(sysConfigClient.getAssets(), new HashMap<>());
    final var init = mig.read("asset_sysconfig_flat.json").orElse(null);
    if(init == null) {
      return Uni.createFrom().nothing();
    }
    final var stencilAssets = mig.readStencil(init);
    final var dialobAssets = mig.readDialob(init);
    final var wrenchAssets = mig.readHdes(init);
    
    return tenantClient.queryActiveTenantConfig().get(currentTenant.tenantId())
        .onItem().transform(config -> sysConfigClient.withTenantConfig(config.getId(), config.getRepoConfigs()))
        .onItem().transformToUni(client -> {
          
          return new StencilComposerImpl(client.getAssets().getConfig().getStencil()).migration().importData(stencilAssets)
          .onItem().transformToUni(site -> {            
            return new HdesComposerImpl(client.getAssets().getConfig().getHdes()).importTag(wrenchAssets).onItem().transform(data -> site);
          })
          .onItem().transformToUni(site -> {            
            return new DialobComposerImpl(client.getAssets().getConfig().getDialob()).create(dialobAssets).onItem().transform(e -> site);
          })
          .onItem().transformToUni(site -> {            
            return client.createConfig().createOne(init.getCommand()).onItem().transform(e -> site);
          });
        });
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("read-assets")
  public Uni<MigrationAssets> readAssets() {
    final var mig = new MigrationClient(sysConfigClient.getAssets(), new HashMap<>());
    final var init = mig.read("asset_sysconfig_flat.json").orElse(null);
    if(init == null) {
      return Uni.createFrom().nothing();
    }
    
    return Uni.createFrom().item(init);
  }
}
