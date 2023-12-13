package io.resys.sysconfig.client.rest;

import java.util.List;

import io.resys.sysconfig.client.api.AssetClient.Asset;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


public interface SysConfigAssetsRestApi {
  
  @GET @Path("sys-configs-assets") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<Asset>> findAllAssets();  
}
