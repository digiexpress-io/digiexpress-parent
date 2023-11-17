package io.resys.crm.client.rest;

import java.util.List;

import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.CustomerCommand.CreateCustomer;
import io.resys.crm.client.api.model.CustomerCommand.CustomerUpdateCommand;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


public interface CrmRestApi {
  
  @GET @Path("tenants") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<Customer>> findCustomers();
  
  @POST @Path("tenants") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<List<Customer>> createCustomer(List<CreateCustomer> commands);
  
  @PUT @Path("tenants") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<List<Customer>> updateCustomer(List<CustomerUpdateCommand> commands);
  

  @PUT @Path("tenants/{tenantConfigId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Customer> updateOneCustomer(@PathParam("tenantConfigId") String crmId, List<CustomerUpdateCommand> commands);
}
