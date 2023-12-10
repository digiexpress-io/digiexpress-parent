package io.resys.sysconfig.client.rest;

import java.util.List;

import io.resys.sysconfig.client.api.model.SysConfigDeployment;
import io.resys.sysconfig.client.api.model.SysConfigDeploymentCommand.CreateSysConfigDeployment;
import io.resys.sysconfig.client.api.model.SysConfigDeploymentCommand.SysConfigDeploymentUpdateCommand;
import io.resys.sysconfig.client.api.model.SysConfigLiveVersion;
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


public interface SysConfigDeploymentRestApi {
  
  @GET @Path("sys-config-deployments") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<SysConfigDeployment>> findAllDeployments();
  
  @GET @Path("sys-config-deployments/{deploymentId}") @Produces(MediaType.APPLICATION_JSON)
  Uni<SysConfigDeployment> getOneDeployment(@PathParam("deploymentId") String deploymentId);
  
  @POST @Path("sys-config-deployments") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<SysConfigDeployment> createOneDeployment(CreateSysConfigDeployment commands);
  
  @PUT @Path("sys-configs-deployments/{deploymentId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<SysConfigDeployment> updateOneDeployment(@PathParam("deploymentId") String deploymentId, List<SysConfigDeploymentUpdateCommand> commands);

  @DELETE @Path("sys-configs-deployments/{deploymentId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<SysConfigDeployment> deleteOneDeployment(@PathParam("deploymentId") String deploymentId, List<SysConfigDeploymentUpdateCommand> command);
  
  
  @GET @Path("sys-config-deployments/live") @Produces(MediaType.APPLICATION_JSON)
  Uni<SysConfigLiveVersion> getLiveVersion();
  
  @GET @Path("sys-config-deployments/next-live") @Produces(MediaType.APPLICATION_JSON)
  Uni<SysConfigDeployment> getNextLiveVersion();
}
