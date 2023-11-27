package io.resys.crm.client.tests.config;

import java.util.Arrays;
import java.util.List;

import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.CustomerCommand.CreateCustomer;
import io.resys.crm.client.api.model.CustomerCommand.CustomerUpdateCommand;
import io.resys.crm.client.api.model.Document.DocumentType;
import io.resys.crm.client.api.model.ImmutableCreateCustomer;
import io.resys.crm.client.api.model.ImmutableCustomer;
import io.resys.crm.client.api.model.ImmutableCustomerTransaction;
import io.resys.crm.client.api.model.ImmutablePerson;
import io.resys.crm.client.rest.CrmRestApi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
@ApplicationScoped
public class CustomerTestResource implements CrmRestApi {

  private final ImmutableCustomer mockCustomer = ImmutableCustomer.builder()
      .id("id-1234")
      .externalId("220276-840H")
      .version("v1.0")
      .created(CustomerTestCase.getTargetDate())
      .updated(CustomerTestCase.getTargetDate())
      .body(ImmutablePerson.builder()
          .firstName("Waldorf")
          .lastName("SaladsMacgoo")
          .username("Waldorf SaladsMacgoo")

          .build())
      .addTransactions(
          ImmutableCustomerTransaction.builder()
          .id("transation-1")
          .addCommands(ImmutableCreateCustomer
              .builder()
              .externalId("220276-840H")
              .body(ImmutablePerson.builder()
                  .firstName("Waldorf")
                  .lastName("SaladsMacgoo")
                  .username("Waldorf SaladsMacgoo")

                  .build())
              .build())
          .build())
      .documentType(DocumentType.CUSTOMER)
      .build();


  @Override
  public Uni<List<Customer>> findAllCustomers() {
    return Uni.createFrom().item(Arrays.asList(mockCustomer));
  }

  @Override
  public Uni<List<Customer>> findAllCustomersByName(String name) {
    return Uni.createFrom().item(Arrays.asList(mockCustomer));
  }

  @Override
  public Uni<Customer> createCustomer(CreateCustomer command) {
    return Uni.createFrom().item(mockCustomer);
  }

  @Override
  public Uni<Customer> updateCustomer(String customerId, List<CustomerUpdateCommand> commands) {
    return Uni.createFrom().item(mockCustomer);
  }

  @Override
  public Uni<Customer> deleteCustomer(String customerId, CustomerUpdateCommand command) {
   return Uni.createFrom().item(mockCustomer);
  }

  @Override
  public Uni<Customer> getCustomerById(String customerId) {
    return Uni.createFrom().item(mockCustomer);
  }
}
