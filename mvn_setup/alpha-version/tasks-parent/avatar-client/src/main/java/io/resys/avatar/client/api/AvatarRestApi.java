package io.resys.avatar.client.api;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


public interface AvatarRestApi {
  
  @GET @Path("/") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<Avatar>> findAllAvatars();
  
  @GET @Path("{externalId}") @Produces(MediaType.APPLICATION_JSON)
  Uni<Avatar> getOrCreateAvatar(@PathParam("externalId") String externalId);

  @POST @Path("/") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<List<Avatar>> getOrCreateAvatars(GetOrCreateAvatars request);
  
  @Value.Immutable @JsonSerialize(as = ImmutableGetOrCreateAvatars.class) @JsonDeserialize(as = ImmutableGetOrCreateAvatars.class)
  interface GetOrCreateAvatars {
    List<String> getId();
  }
}
