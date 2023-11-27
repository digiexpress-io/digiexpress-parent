package io.resys.thena.tasks.dev.app;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.crm.client.api.CrmClient;
import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.CustomerCommand.CreateCustomer;
import io.resys.crm.client.api.model.CustomerCommand.CustomerUpdateCommand;
import io.resys.crm.client.rest.CrmRestApi;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.dev.app.BeanFactory.CurrentTenant;
import io.resys.thena.tasks.dev.app.BeanFactory.CurrentUser;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
public class CrmResource implements CrmRestApi {

  @Inject CrmClient crmClient;
  @Inject CurrentTenant currentTenant;
  @Inject CurrentUser currentUser;
  @Inject TenantConfigClient tenantClient;
  
  @Override
  public Uni<List<Customer>> findAllCustomers() {
    return getCrmConfig().onItem().transformToUni(config -> crmClient.withRepoId(config.getRepoId()).customerQuery().findAll());
  }

  @Override
  public Uni<Customer> getCustomerById(String customerId) {
    return getCrmConfig().onItem().transformToUni(config -> crmClient.withRepoId(config.getRepoId()).customerQuery().get(customerId));
  }
  @Override
  public Uni<List<Customer>> findAllCustomersByName(String name) {
    return getCrmConfig().onItem().transformToUni(config -> crmClient.withRepoId(config.getRepoId()).customerQuery().findAll());
  }

  @Override
  public Uni<Customer> createCustomer(CreateCustomer command) {
    return getCrmConfig().onItem().transformToUni(config -> crmClient.withRepoId(config.getRepoId()).createCustomer()
        .createOne((CreateCustomer) command.withTargetDate(Instant.now()).withUserId(currentUser.getUserId())));
  }

  @Override
  public Uni<Customer> updateCustomer(String customerId, List<CustomerUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command.withTargetDate(Instant.now()).withUserId(currentUser.getUserId()))
        .collect(Collectors.toList());
    
    return getCrmConfig().onItem().transformToUni(config -> crmClient.withRepoId(config.getRepoId()).updateCustomer()
        .updateOne(modifiedCommands));
  }

  @Override
  public Uni<Customer> deleteCustomer(String customerId, CustomerUpdateCommand command) {
    return getCrmConfig().onItem().transformToUni(config -> crmClient.withRepoId(config.getRepoId()).updateCustomer()
        .updateOne(command.withTargetDate(Instant.now()).withUserId(currentUser.getUserId())));
  }

  private Uni<TenantRepoConfig> getCrmConfig() {
    return tenantClient.queryActiveTenantConfig().get(currentTenant.getTenantId())
    .onItem().transform(config -> {
      final var crmConfig = config.getRepoConfigs().stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.CRM).findFirst().get();
      return crmConfig;
    });
  }

}
