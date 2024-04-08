package io.resys.thena.tasks.dev.app;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.LevenshteinDistance;

import io.resys.crm.client.api.CrmClient;
import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.Customer.CustomerBodyType;
import io.resys.crm.client.api.model.Customer.Person;
import io.resys.crm.client.api.model.CustomerCommand.CreateCustomer;
import io.resys.crm.client.api.model.CustomerCommand.CustomerUpdateCommand;
import io.resys.crm.client.rest.CrmRestApi;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
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
    return getCrmConfig()
        .onItem().transformToUni(config -> crmClient.withRepoId(config.getRepoId()).customerQuery().findAll())
        .onItem().transform(customers -> {
          final var criteria = createNames(name);
          return customers.stream().filter(customer -> isMatch(criteria, customer)).collect(Collectors.toList());  
        });
  }
  
  private boolean isMatch(String[] criteria, Customer customer) {
    final var customerNames = new ArrayList<String>();
    customerNames.addAll(Arrays.asList(createNames(customer.getBody().getUsername())));
    
    if(customer.getBody().getType() == CustomerBodyType.PERSON) {
      final var person = (Person) customer.getBody();
      customerNames.addAll(Arrays.asList(createNames(person.getFirstName())));
      customerNames.addAll(Arrays.asList(createNames(person.getLastName())));
    }
    

    boolean isMatch = false;
    for(final var crit : criteria) {
      if(crit.equals(customer.getExternalId().toUpperCase())) {
        return true;
      }

      boolean isAtLeastOneNameMatch = false;
      for(final var name : customerNames) {
        final var diff = LevenshteinDistance.getDefaultInstance().apply(name, crit);
        if(diff < 3) {
          isAtLeastOneNameMatch = true;
          break;
        }
      }
      
      if(!isAtLeastOneNameMatch) {
        isMatch = false;
        break;
      }
      isMatch = true;
    }
    
    return isMatch;
  }
  
  private String[] createNames(String name) {
    return name.trim().replace("  ", " ").toUpperCase().split(" ");
  }

  @Override
  public Uni<Customer> createCustomer(CreateCustomer command) {
    return getCrmConfig().onItem().transformToUni(config -> crmClient.withRepoId(config.getRepoId()).createCustomer()
        .createOne((CreateCustomer) command.withTargetDate(Instant.now()).withUserId(currentUser.userId())));
  }

  @Override
  public Uni<Customer> updateCustomer(String customerId, List<CustomerUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command.withTargetDate(Instant.now()).withUserId(currentUser.userId()))
        .collect(Collectors.toList());
    
    return getCrmConfig().onItem().transformToUni(config -> crmClient.withRepoId(config.getRepoId()).updateCustomer()
        .updateOne(modifiedCommands));
  }

  @Override
  public Uni<Customer> deleteCustomer(String customerId, CustomerUpdateCommand command) {
    return getCrmConfig().onItem().transformToUni(config -> crmClient.withRepoId(config.getRepoId()).updateCustomer()
        .updateOne(command.withTargetDate(Instant.now()).withUserId(currentUser.userId())));
  }

  private Uni<TenantRepoConfig> getCrmConfig() {
    return tenantClient.queryActiveTenantConfig().get(currentTenant.tenantId())
    .onItem().transform(config -> {
      final var crmConfig = config.getRepoConfigs().stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.CRM).findFirst().get();
      return crmConfig;
    });
  }

}
