package io.resys.sysconfig.client.rest;

import io.dialob.api.proto.Actions;
import io.resys.sysconfig.client.api.ExecutorClient.SysConfigSession;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder.Sites;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


public interface SysConfigSimulationRestApi {

  enum SimulationRequestIdType { RELEASE, CONFIG }
  
  @GET @Path("sys-configs-sim/sites/{id}/{type}") @Produces(MediaType.APPLICATION_JSON)
  Uni<Sites> getOneSite(
      @PathParam("id") String id, 
      @PathParam("type") SimulationRequestIdType type);

  @POST @Path("sys-configs-sim/workflows/{id}/{type}/{workflowName}") @Produces(MediaType.APPLICATION_JSON)
  Uni<SysConfigSession> createOneSession(
      @PathParam("id") String id, 
      @PathParam("type") SimulationRequestIdType type, 
      @PathParam("workflowName") String workflowName);
  
  @POST @Path("sys-configs-sim/{sysConfigSessionId}/sessions/{sessionId}") @Produces(MediaType.APPLICATION_JSON)
  Uni<SysConfigSession> fillOneSession(
      @PathParam("sysConfigSessionId") String sysConfigSessionId, 
      @PathParam("sessionId") String sessionId, 
      Actions actions);
  
  @GET @Path("sys-configs-sim/{sysConfigSessionId}") @Produces(MediaType.APPLICATION_JSON)
  Uni<SysConfigSession> getOneSession(
      @PathParam("sysConfigSessionId") String sysConfigSessionId);
}
