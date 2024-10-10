package io.resys.sysconfig.client.rest;

import java.util.List;

import io.resys.sysconfig.client.api.model.SysConfig;
import io.resys.sysconfig.client.api.model.SysConfigCommand.CreateSysConfig;
import io.resys.sysconfig.client.api.model.SysConfigCommand.SysConfigUpdateCommand;
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


public interface SysConfigRestApi {
  
  @GET @Path("sys-configs") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<SysConfig>> findAllSysConfigs();
 
  @GET @Path("sys-configs/{sysConfigId}") @Produces(MediaType.APPLICATION_JSON)
  Uni<SysConfig> getOneSysConfig(@PathParam("sysConfigId") String sysConfigId);
  
  @POST @Path("sys-configs") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<SysConfig> createOneSysConfig(CreateSysConfig commands);
  
  @PUT @Path("sys-configs/{sysConfigId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<SysConfig> updateOneSysConfig(@PathParam("sysConfigId") String sysConfigId, List<SysConfigUpdateCommand> commands);

  @DELETE @Path("sys-configs/{sysConfigId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<SysConfig> deleteOneSysConfig(@PathParam("sysConfigId") String sysConfigId, List<SysConfigUpdateCommand> command);
  
}
