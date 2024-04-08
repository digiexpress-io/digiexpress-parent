package io.resys.thena.tasks.dev.app;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.resys.thena.tasks.dev.app.user.CurrentSetup;
import io.resys.thena.tasks.dev.app.user.CurrentUserConfig;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("q/digiexpress/api")
public class ConfigResource {
  
  @Inject JsonWebToken jwt;
  @Inject CurrentSetup setup;
  
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("config/current-user")
  public Uni<CurrentUserConfig> currentUserConfig() {
    return setup.getOrCreateCurrentUserConfig();
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
