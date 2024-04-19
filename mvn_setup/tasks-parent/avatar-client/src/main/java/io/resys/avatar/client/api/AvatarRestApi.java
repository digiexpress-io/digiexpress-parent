package io.resys.avatar.client.api;

import java.util.List;

import io.resys.avatar.client.api.AvatarCommand.AvatarUpdateCommand;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


public interface AvatarRestApi {
  
  @GET @Path("avatars") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<Avatar>> findAllAvatars();
  
  @GET @Path("avatars/{externalId}") @Produces(MediaType.APPLICATION_JSON)
  Uni<Avatar> getOrCreateAvatar(@PathParam("externalId") String externalId);

  @PUT @Path("avatars/{avatarId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Avatar> updateUserProfile(@PathParam("avatarId") String avatarId, List<AvatarUpdateCommand> commands);
   
}
