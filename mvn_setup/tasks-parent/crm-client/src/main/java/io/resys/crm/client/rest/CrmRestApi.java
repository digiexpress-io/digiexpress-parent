package io.resys.crm.client.rest;

import java.util.List;

import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.CustomerCommand.CreateCustomer;
import io.resys.crm.client.api.model.CustomerCommand.CustomerUpdateCommand;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;


public interface CrmRestApi {
  
  @GET @Path("customers") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<Customer>> findAllCustomers();
  

  @GET @Path("customers/{customerId}") @Produces(MediaType.APPLICATION_JSON)
  Uni<Customer> getCustomerById(@PathParam("customerId") String customerId);
  
  @GET @Path("customers/search") @Produces(MediaType.APPLICATION_JSON) 
  Uni<List<Customer>> findAllCustomersByName(@QueryParam("name") String name);
  
  @POST @Path("customers") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Customer> createCustomer(CreateCustomer command);

  @PUT @Path("customers/{customerId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Customer> updateCustomer(@PathParam("customerId") String customerId, List<CustomerUpdateCommand> commands);
  
  @DELETE @Path("customers/{customerId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Customer> deleteCustomer(@PathParam("customerId") String customerId, CustomerUpdateCommand command);
  
}
