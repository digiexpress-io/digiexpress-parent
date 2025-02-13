package io.resys.thena.tasks.dev.app;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.dialob.client.spi.DialobComposerImpl;
import io.resys.crm.client.api.CrmClient;
import io.resys.crm.client.api.model.Customer;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableCreatePermission;
import io.resys.permission.client.api.model.ImmutableCreateRole;
import io.resys.sysconfig.client.api.SysConfigClient;
import io.resys.sysconfig.client.mig.MigrationClient;
import io.resys.sysconfig.client.mig.model.MigrationAssets;
import io.resys.thena.projects.client.api.ImmutableCreateTenantConfig;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.TenantConfig;
import io.resys.thena.projects.client.api.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.dev.app.demo.DemoOrg;
import io.resys.thena.tasks.dev.app.demo.RandomDataProvider;
import io.resys.thena.tasks.dev.app.demo.TaskGen;
import io.resys.thena.tasks.dev.app.security.BuiltInDataPermissions;
import io.resys.thena.tasks.dev.app.security.BuiltInRoles;
import io.resys.thena.tasks.dev.app.security.BuiltInUIPermissions;
import io.resys.thena.tasks.dev.app.security.IdentitySupplier;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.resys.thena.tasks.dev.app.user.CurrentUserConfig;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.spi.StencilComposerImpl;
import io.vertx.mutiny.core.Vertx;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("q/demo/api/")
@Singleton
public class DemoResource {
  @Inject Vertx vertx;
  @Inject PermissionClient permissions;
  @Inject TaskClient taskClient;
  @Inject CrmClient crmClient;
  @Inject ProjectClient tenantClient;
  @Inject CurrentTenant currentTenant;
  @Inject SysConfigClient sysConfigClient;
  @Inject IdentitySupplier setup;
  @Inject CurrentUser currentUser;
  @Inject DemoOrg demoOrg;
  

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("populate/tasks/{totalTasks}")
  public Uni<TenantConfig> populate(@PathParam("totalTasks") String totalTasks) {
    final var provider =  new RandomDataProvider();
    final var windows = provider.windows(totalTasks == null ? 50 : Integer.parseInt(totalTasks));

    return tenantClient.queryActiveTenantConfig().get(currentTenant.tenantId())
    .onItem().transformToUni(config -> {
      final var crm = this.crmClient.withRepoId(config.getRepoConfig(TenantRepoConfigType.CRM).getRepoId());
      final var tasks = this.taskClient.withRepoId(config.getRepoConfig(TenantRepoConfigType.TASKS).getRepoId());
      final var permissions = this.permissions.withRepoId(config.getRepoConfig(TenantRepoConfigType.PERMISSIONS).getRepoId());
      
      return Uni.combine().all()
      .unis(
          permissions.principalQuery().findAllPrincipals(),
          permissions.roleQuery().findAllRoles()
       ).asTuple().onItem().transformToUni(tuple -> {
         
         return crm.createCustomer().upsertMany(new TaskGen().generateCustomers(windows))
         .onItem().transformToUni((List<Customer> data) -> {
           final var bulkTasks = new TaskGen().generateTasks(windows, data, tuple);
           return tasks.tasks().createTask().createMany(bulkTasks);
         });
       })
      .onItem().transform(junk -> config);
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
  public Uni<CurrentUserConfig> reinit() {
    return tenantClient.query().deleteAll()
        .onItem().transformToUni(junk -> createTenant())
        .onItem().transformToUni(tenant -> createRoles(tenant).onItem().transformToUni(junk -> createPermissions(tenant)))
        
        .onItem().transformToUni(junk -> demoOrg.generate())
        .onItem().transformToUni(junk -> setup.invalidate())
        .onItem().transformToUni(junk -> setup.getOrCreateCurrentUserConfig(currentUser))
        ;
  }
  
  
  private Uni<Void> createRoles(TenantConfig tenant) {
    final var config = tenant.getRepoConfig(TenantRepoConfigType.PERMISSIONS);
    final var client = permissions.withRepoId(config.getRepoId());
    
    return Multi.createFrom().items(BuiltInRoles.values())
        .onItem().transformToUni(role -> {
          return  client.roleQuery().get(role.name())
            .onFailure().recoverWithUni(e -> {
              log.warn("App setup, system role: '{}' not defined, trying to create it!", role.name());
              return client.createRole().createOne(ImmutableCreateRole.builder()
                  .comment("created by default on first user registration")
                  .name(role.name())
                  .description(role.getDescription())
                  .build());
            });
        })
        .concatenate().collect().asList()
        .onItem().transformToUni(junk -> Uni.createFrom().voidItem());
  }
  
  private Uni<Void> createPermissions(TenantConfig tenant) {
    final var config = tenant.getRepoConfig(TenantRepoConfigType.PERMISSIONS);
    final var client = permissions.withRepoId(config.getRepoId());
    
    return Multi.createFrom().items(BuiltInDataPermissions.values())
      .onItem().transformToUni(permission -> {
        // default data permissions
        final var command = ImmutableCreatePermission.builder()
            .name(permission.name())
            .description(permission.getDescription())
            .comment("created by default");
        
        if(permission == BuiltInDataPermissions.DATA_TENANT_READ ||
            permission == BuiltInDataPermissions.DATA_PERMISSIONS_READ ) {
          command.addAllRoles(Arrays.asList(BuiltInRoles.LOBBY.name())); 
        }
        
        return client.createPermission().createOne(command.build());
      })
      .concatenate().collect().asList()
      .onItem().transformToMulti(junk_ -> Multi.createFrom().items(BuiltInUIPermissions.values()))
      .onItem().transformToUni(permission -> {
        // default ui permissions
        final var command = ImmutableCreatePermission.builder()
            .name(permission.name())
            .description(permission.getDescription())
            .comment("created by default");

        return client.createPermission().createOne(command.build());
      }).concatenate().collect().asList().onItem().transformToUni(e -> Uni.createFrom().voidItem());
    
  }
  
  
  
  private Uni<TenantConfig> createTenant() {
    return tenantClient.query().repoName(currentTenant.tenantsStoreId(), TenantRepoConfigType.TENANT).createIfNot()
        .onItem().transformToUni(created -> tenantClient.createTenantConfig().createOne(ImmutableCreateTenantConfig.builder()
            .repoId(currentTenant.tenantsStoreId())
            .name(currentTenant.tenantId())
            .build()))
        .onItem().transformToUni(tenantConfig -> {
          return Multi.createFrom().items(tenantConfig.getRepoConfigs().stream())
            .onItem().transformToUni(child -> tenantClient.query().repoName(child.getRepoId(), child.getRepoType()).createIfNot())
            .concatenate().collect().asList()
            .onItem().transform(junk -> tenantConfig);
        });
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
