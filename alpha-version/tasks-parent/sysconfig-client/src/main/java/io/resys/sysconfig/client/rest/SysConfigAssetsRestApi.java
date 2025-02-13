package io.resys.sysconfig.client.rest;

import java.util.List;

import io.resys.sysconfig.client.api.AssetClient.AssetSource;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


public interface SysConfigAssetsRestApi {
  
  @GET @Path("sys-configs-asset-sources") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<AssetSource>> findAllAssetSources();  
  
  @GET @Path("sys-configs-asset-sources/wrench/{sysconfig-id}") @Produces(MediaType.APPLICATION_JSON)
  Uni<io.resys.hdes.client.api.HdesComposer.ComposerState> findWrenchAssets(@PathParam("sysconfig-id") String sysConfigId);
  
  @GET @Path("sys-configs-asset-sources/stencil/{sysconfig-id}") @Produces(MediaType.APPLICATION_JSON)
  Uni<io.thestencil.client.api.StencilClient.Release> findStencilAssets(@PathParam("sysconfig-id") String sysConfigId);
}
