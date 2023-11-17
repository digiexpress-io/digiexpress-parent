package io.resys.crm.client.api;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.CustomerCommand.CreateCustomer;
import io.resys.crm.client.api.model.CustomerCommand.CustomerUpdateCommand;
import io.resys.thena.docdb.api.models.Repo;
import io.smallrye.mutiny.Uni;

public interface CrmClient {

  
  RepositoryQuery repoQuery();
  Uni<Repo> getRepo();
  
  CreateCustomerAction createCustomer();
  UpdateCustomerAction updateCustomer();
  CustomerQuery customerQuery();

  interface CreateCustomerAction {
    Uni<Customer> createOne(CreateCustomer command);
    Uni<List<Customer>> createMany(List<? extends CreateCustomer> commands);
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
    Uni<List<Customer>> deleteAll(String userId, Instant targetDate);
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

}
