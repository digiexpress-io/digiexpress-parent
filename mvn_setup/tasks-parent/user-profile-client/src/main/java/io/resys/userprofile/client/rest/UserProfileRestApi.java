package io.resys.userprofile.client.rest;

import java.util.List;

import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.UserProfileUpdateCommand;
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


public interface UserProfileRestApi {
  
  @GET @Path("userprofiles") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<UserProfile>> findAllUserProfiles();
  
  @GET @Path("userprofiles/{profileId}") @Produces(MediaType.APPLICATION_JSON)
  Uni<UserProfile> getUserProfileById(@PathParam("profileId") String profileId);
  
  @GET @Path("userprofiles/search") @Produces(MediaType.APPLICATION_JSON) 
  Uni<List<UserProfile>> findAllUserProfilesByName(@QueryParam("name") String name);
  
  @POST @Path("userprofiles") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<UserProfile> createUserProfile(CreateUserProfile command);

  @PUT @Path("userprofiles/{profileId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<UserProfile> updateUserProfile(@PathParam("profileId") String profileId, List<UserProfileUpdateCommand> commands);
  
  @DELETE @Path("userprofiles/{profileId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<UserProfile> deleteUserProfile(@PathParam("profileId") String profileId, UserProfileUpdateCommand command);
  
}
