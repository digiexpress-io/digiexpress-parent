package io.resys.thena.projects.client.rest;

import java.util.List;

import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.ArchiveTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.CreateTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.TenantConfigUpdateCommand;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


public interface TenantConfigRestApi {
  
  @GET @Path("tenants") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<TenantConfig>> findTenantConfigs();
  
  @POST @Path("tenants") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<List<TenantConfig>> createTenantConfigs(List<CreateTenantConfig> commands);
  
  @PUT @Path("tenants") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<List<TenantConfig>> updateTenantConfigs(List<TenantConfigUpdateCommand> commands);
  
  @DELETE @Path("tenants") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<List<TenantConfig>> deleteTenantConfigs(List<ArchiveTenantConfig> commands);

  @PUT @Path("tenants/{tenantConfigId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<TenantConfig> updateOneTenantConfig(@PathParam("tenantConfigId") String tenantConfigId, List<TenantConfigUpdateCommand> commands);
}
