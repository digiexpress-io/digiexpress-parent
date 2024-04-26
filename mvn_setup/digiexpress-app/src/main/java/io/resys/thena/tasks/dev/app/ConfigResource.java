package io.resys.thena.tasks.dev.app;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.resys.thena.tasks.dev.app.user.CurrentSetup;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.resys.thena.tasks.dev.app.user.CurrentUserConfig;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("q/digiexpress/api")
@Singleton
public class ConfigResource {
  
  @Inject JsonWebToken jwt;
  @Inject CurrentSetup setup;
  @Inject CurrentUser currentUser;
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("config/current-user")
  public Uni<CurrentUserConfig> currentUserConfig() {
    return setup.getOrCreateCurrentUserConfig(currentUser);
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("config/health")
  public Uni<Void> health() {
    return Uni.createFrom().voidItem();
  }
   
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("config/jwt")
  public Uni<JsonObject> currentJwt() {
    Map<String, Object> result = new HashMap<>();
    jwt.getClaimNames().forEach(claim -> result.put(claim, jwt.getClaim(claim)));
    return Uni.createFrom().item(JsonObject.mapFrom(result));
  } 

}
