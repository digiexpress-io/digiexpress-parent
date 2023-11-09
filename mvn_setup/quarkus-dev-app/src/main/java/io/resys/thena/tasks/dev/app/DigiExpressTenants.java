package io.resys.thena.tasks.dev.app;

import java.util.Collections;
import java.util.List;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.Data;

@Path("q/digiexpress/api")
public class DigiExpressTenants {

  @Data
  public static class DialobTenant {
    
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("api/tenants")
  public Uni<List<DialobTenant>> findTenants() {
    return Uni.createFrom().item(Collections.emptyList());
  }

}
