package io.resys.thena.tasks.dev.app;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import io.dialob.client.spi.DialobComposerImpl;
import io.resys.crm.client.api.CrmClient;
import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.CustomerCommand.UpsertSuomiFiPerson;
import io.resys.crm.client.api.model.ImmutableUpsertSuomiFiPerson;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.sysconfig.client.api.SysConfigClient;
import io.resys.sysconfig.client.mig.MigrationClient;
import io.resys.sysconfig.client.mig.model.MigrationAssets;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.ImmutableCreateTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.ImmutableTaskExtension;
import io.resys.thena.tasks.client.api.model.Task.TaskExtensionType;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.dev.app.demo.RandomDataProvider;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.smallrye.mutiny.Multi;
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

      return crmClient.withRepoId(crmConfig.getRepoId()).createCustomer().upsertMany(generateCustomers(windows))
          .onItem().transformToUni((List<Customer> data) -> {
            final var bulkTasks = generateTasks(windows, data);
            return taskClient.withRepoId(taskConfig.getRepoId()).tasks().createTask().createMany(bulkTasks);
          }).onItem().transform(junk -> config);
    });
    
    
    /*
    final var bulkCustomers = new ArrayList<UpsertSuomiFiPerson>();
    final var bulkTasks = new ArrayList<CreateTask>();
    final var targetDate = Instant.now();
    
    UpsertSuomiFiPerson customer;
    for(int index = 0; index < count; index++) {

      if() {
        final var person = provider.person();
        crmClient.createCustomer().createOne(ImmutableUpsertSuomiFiPerson.builder()
            .customerId(provider.ssn())
            .userName(person.getUsername())
            .firstName(person.getFirstName())
            .lastName(person.getLastName())
            .protectionOrder(person.getProtectionOrder())
            .contact(person.getContact())
            .build());
      }
      
      final var startAndDueDate = provider.getStartDateAndDueDate(LocalDate.ofInstant(targetDate, ZoneId.of("UTC")));
      final var newTask = ImmutableCreateTask.builder()
        .startDate(startAndDueDate.getStartDate())
        .dueDate(startAndDueDate.getDueDate())
        .targetDate(targetDate.minus(10, java.time.temporal.ChronoUnit.DAYS))
        .checklist(provider.getChecklists(LocalDate.ofInstant(targetDate, ZoneId.of("UTC"))))
        .title(provider.getTitle())
        .description(provider.getDescription())
        .priority(provider.getPriority())
        .roles(provider.getRoles())
        .assigneeIds(provider.getAssigneeIds())
        .reporterId(provider.getReporterId())
        .status(provider.getStatus())
        .userId("demo-gen-1")
        .addAllExtensions(provider.getExtensions())
        .comments(provider.getComments())
        .build();
      bulkTasks.add(newTask);
    }

    
    return tenantClient.queryActiveTenantConfig().get(currentTenant.getTenantId())
    .onItem().transformToUni(config -> {
      final var taskConfig = config.getRepoConfigs().stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.TASKS).findFirst().get();
      
      
      
      return taskClient.withRepoId(taskConfig.getRepoId()).tasks().createTask().createMany(bulkTasks).onItem().transform((data) -> config);
    });
   */ 
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
        //.onItem().transformToUni(config -> reinitAssets().onItem().transform(assets -> config));
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
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("kill9")
  public Uni<Void> kill9() {
    return tenantClient.query().deleteAll()
        .onItem().transformToUni(junk -> Uni.createFrom().voidItem());
  }
  private Uni<TenantConfig> init() {
    return tenantClient.query().repoName(currentTenant.tenantsStoreId(), TenantRepoConfigType.TENANT).createIfNot()
        .onItem().transformToUni(created -> {
          return tenantClient.createTenantConfig().createOne(ImmutableCreateTenantConfig.builder()
              .name(currentTenant.tenantId())
              .repoId(currentTenant.tenantsStoreId())
              .targetDate(Instant.now())
              .build())
              .onItem().transformToUni(this::createNested);
        });
  }

  private Uni<TenantConfig> createNested(TenantConfig tenant) {
    return Multi.createFrom().items(tenant.getRepoConfigs().stream())
      .onItem().transformToUni(this::createRepo)
      .concatenate().collect().asList().onItem().transform(junk -> tenant);
  }
  
  private Uni<TenantRepoConfig> createRepo(TenantRepoConfig config) {
    return tenantClient.query()
    .repoName(config.getRepoId(), config.getRepoType())
    .createIfNot().onItem().transform(created -> config);
  }
  
  
  public List<UpsertSuomiFiPerson> generateCustomers(List<Integer> windows) {
    final var targetDate = Instant.now();
    final var provider =  new RandomDataProvider();  
    return windows.stream().map(junk -> {
      final var person = provider.person();
      return ImmutableUpsertSuomiFiPerson.builder()
          .userId("demo-gen-1")
          .targetDate(targetDate)
          .customerId(provider.ssn())
          .userName(person.getUsername())
          .firstName(person.getFirstName())
          .lastName(person.getLastName())
          .protectionOrder(person.getProtectionOrder())
          .contact(person.getContact())
          .build();
    }).collect(Collectors.toList());
  }
  
  
  public List<CreateTask> generateTasks(List<Integer> windows, List<Customer> customers) {
    
    final var targetDate = Instant.now();
    final var provider =  new RandomDataProvider();  
    final var windowsIt = windows.iterator();
    final var customersIt = customers.iterator();
    final var bulkTasks = new ArrayList<CreateTask>();

    while(customersIt.hasNext()) {
      final var customer = customersIt.next();
      final var count = windowsIt.next();
      for(int index = 0; index < count; index++) {
        
        final var startAndDueDate = provider.getStartDateAndDueDate(LocalDate.ofInstant(targetDate, ZoneId.of("UTC")));
        final var newTask = ImmutableCreateTask.builder()
          .startDate(startAndDueDate.getStartDate())
          .dueDate(startAndDueDate.getDueDate())
          .targetDate(targetDate.minus(10, java.time.temporal.ChronoUnit.DAYS))
          .checklist(provider.getChecklists(LocalDate.ofInstant(targetDate, ZoneId.of("UTC"))))
          .title(provider.getTitle())
          .description(provider.getDescription())
          .priority(provider.getPriority())
          .roles(provider.getRoles())
          .assigneeIds(provider.getAssigneeIds())
          .reporterId(provider.getReporterId())
          .status(provider.getStatus())
          .userId("demo-gen-1")
          .addAllExtensions(provider.getExtensions())
          .comments(provider.getComments())
          .addExtensions(ImmutableTaskExtension.builder()
              .id(customer.getId())
              .created(targetDate)
              .updated(targetDate)
              .type(TaskExtensionType.CUSTOMER.name())
              .name("crm-link")
              .body(customer.getId())
              .build())
          .build();
        bulkTasks.add(newTask);
      }
    }
    
    return bulkTasks;
  }
}
