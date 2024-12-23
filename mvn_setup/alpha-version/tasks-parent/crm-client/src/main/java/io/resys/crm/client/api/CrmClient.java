package io.resys.crm.client.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.CustomerCommand.CreateCustomer;
import io.resys.crm.client.api.model.CustomerCommand.CustomerUpdateCommand;
import io.resys.crm.client.api.model.CustomerCommand.UpsertSuomiFiPerson;
import io.resys.thena.api.entities.Tenant;
import io.smallrye.mutiny.Uni;

public interface CrmClient {

  RepositoryQuery repoQuery();
  Uni<Tenant> getRepo();
  CrmClient withRepoId(String repoId);
  
  CreateCustomerAction createCustomer();
  UpdateCustomerAction updateCustomer();
  CustomerQuery customerQuery();

  interface CreateCustomerAction {
    Uni<Customer> createOne(UpsertSuomiFiPerson command);
    Uni<Customer> createOne(CreateCustomer command);
    Uni<List<Customer>> createMany(List<? extends CreateCustomer> commands);
    Uni<List<Customer>> upsertMany(List<? extends UpsertSuomiFiPerson> commands);
  }

  interface UpdateCustomerAction {
    Uni<Customer> updateOne(CustomerUpdateCommand command);
    Uni<Customer> updateOne(List<CustomerUpdateCommand> commands);
    Uni<List<Customer>> updateMany(List<CustomerUpdateCommand> commands);
  }

  interface CustomerQuery {
    Uni<List<Customer>> findAll();
    Uni<List<Customer>> findByIds(Collection<String> crmIds);
    Uni<Customer> get(String crmId);
    Uni<List<Customer>> deleteAll();
  }
  
  public interface RepositoryQuery {
    RepositoryQuery repoName(String repoName);
    CrmClient build();

    Uni<CrmClient> deleteAll();
    Uni<CrmClient> delete();
    Uni<CrmClient> create();
    Uni<CrmClient> createIfNot();
    
    Uni<Optional<CrmClient>> get(String repoId);
  } 
  class CustomerNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 5706579544456750293L;

    public CustomerNotFoundException(String message) {
      super(message);
    }
  }
}
