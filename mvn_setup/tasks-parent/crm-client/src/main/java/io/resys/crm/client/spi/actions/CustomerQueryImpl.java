package io.resys.crm.client.spi.actions;

import java.util.Collection;
import java.util.List;

import io.resys.crm.client.api.CrmClient.CustomerQuery;
import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.spi.store.CrmStore;
import io.resys.crm.client.spi.visitors.DeleteAllCustomersVisitor;
import io.resys.crm.client.spi.visitors.FindAllCustomersVisitor;
import io.resys.crm.client.spi.visitors.GetActiveCustomerVisitor;
import io.resys.crm.client.spi.visitors.GetCustomersByIdsVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CustomerQueryImpl implements CustomerQuery {
  private final CrmStore ctx;
  
  @Override
  public Uni<Customer> get(String id) {
    return ctx.getConfig().accept(new GetActiveCustomerVisitor(id));
  }
  
  @Override
  public Uni<List<Customer>> findAll() {
    return ctx.getConfig().accept(new FindAllCustomersVisitor());
  }

  @Override
  public Uni<List<Customer>> deleteAll() {
    return ctx.getConfig().accept(new DeleteAllCustomersVisitor())
        .onItem().transformToUni(unwrap -> unwrap);
  }
  
  @Override
  public Uni<List<Customer>> findByIds(Collection<String> customerIds) {
    return ctx.getConfig().accept(new GetCustomersByIdsVisitor(customerIds));
  }
}
