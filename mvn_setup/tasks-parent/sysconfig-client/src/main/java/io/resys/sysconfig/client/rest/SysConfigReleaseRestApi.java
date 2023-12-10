package io.resys.sysconfig.client.rest;

import java.util.List;

import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


public interface SysConfigReleaseRestApi {
  
  @GET @Path("sys-config-releases") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<SysConfigRelease>> findAllReleases();
 
  @DELETE @Path("sys-config-releases/{releaseId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<SysConfigRelease> deleteOneRelease(@PathParam("releaseId") String releaseId);
  
}
