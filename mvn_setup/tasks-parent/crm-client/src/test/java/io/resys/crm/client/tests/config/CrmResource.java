package io.resys.crm.client.tests.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.CustomerCommand.CreateCustomer;
import io.resys.crm.client.api.model.CustomerCommand.CustomerCommandType;
import io.resys.crm.client.api.model.CustomerCommand.CustomerUpdateCommand;
import io.resys.crm.client.api.model.Document.DocumentType;
import io.resys.crm.client.api.model.ImmutableCreateCustomer;
import io.resys.crm.client.api.model.ImmutableCustomer;
import io.resys.crm.client.api.model.ImmutableCustomerTransaction;
import io.resys.crm.client.rest.CrmRestApi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
@ApplicationScoped
public class CrmResource implements CrmRestApi {

  private final ImmutableCustomer mockCustomer = ImmutableCustomer.builder()
      .id("crm-1")
      .name("abc-company")
      .version("v1.0")
      .created(ProjectTestCase.getTargetDate())
      .updated(ProjectTestCase.getTargetDate())
      .addTransactions(
          ImmutableCustomerTransaction.builder()
          .id("transation-1")
          .addCommands(ImmutableCreateCustomer
              .builder()
              .commandType(CustomerCommandType.CreateCustomer)
              .repoId("repo-1")
              .name("customer-1")
              .build())
          .build())
      .documentType(DocumentType.CRM)
      .build();

  @Override
  public Uni<List<Customer>> findCustomers() {
    return Uni.createFrom()
        .item(Arrays.asList(mockCustomer));
  }

  @Override
  public Uni<List<Customer>> updateCustomer(List<CustomerUpdateCommand> commands) {
    return Uni.createFrom().item(commands.stream().map(e -> mockCustomer).collect(Collectors.toList()));
  }

  @Override
  public Uni<Customer> updateOneCustomer(String crmId, List<CustomerUpdateCommand> commands) {
    return Uni.createFrom().item(mockCustomer);
  }

  @Override
  public Uni<List<Customer>> createCustomer(List<CreateCustomer> commands) {
    return Uni.createFrom().item(commands.stream().map(e -> mockCustomer).collect(Collectors.toList()));
  }
}
