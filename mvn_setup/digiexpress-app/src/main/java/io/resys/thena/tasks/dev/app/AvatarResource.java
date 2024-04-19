package io.resys.thena.tasks.dev.app;

import io.resys.hdes.client.api.HdesComposer.StoreDump;
import io.resys.permission.client.api.PermissionClient;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.tasks.dev.app.security.PrincipalCache;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.resys.userprofile.client.api.UserProfileClient;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("q/digiexpress/api/avatars")
public class AvatarResource {

  @Inject PermissionClient permissions;
  @Inject CurrentTenant currentTenant;
  @Inject CurrentUser currentUser;
  @Inject TenantConfigClient tenantClient;
  @Inject UserProfileClient userProfileClient;
  @Inject PrincipalCache cache;
  
  @lombok.Data @lombok.Builder @lombok.extern.jackson.Jacksonized
  public static class Avatar {
    private final String id; //safe to use id, does not expose any private data 
    private final String colorCode;
    private final String letterCode;
    private final String classifierCode;
    private final String displayName; // safe to use, is checked against permissions
    private final AvatarType avatarType;
  }
  
  public static enum AvatarType {
    CRM, ROLE, USER
  }
  /*
  @GET @Path("users/{userId}") @Produces(MediaType.APPLICATION_JSON)
  public Uni<Avatar> users(@PathParam("userId") String userId) {
    return userProfileClient.userProfileQuery().get(userId).onItem().transform(profile -> {
      return Avatar.builder()
          .avatarType(AvatarType.USER)
          .id(userId)
          .displayName(profile.getDetails().getDisplayName())
          .letterCode(profile.getDetails().getLetterCode())
          .classifierCode(profile.getDetails().getUsername())
          .colorCode(profile.getDetails().getColorCode())
          .build();
    });
  }
  
  @GET @Path("roles/{roleId}") @Produces(MediaType.APPLICATION_JSON)
  public Uni<Avatar> roles(@PathParam("roleId") String roleId) {
    return getComposer().onItem().transformToUni(composer -> composer.getStoreDump());
  }
  
  
  @GET @Path("crm/{clientId}") @Produces(MediaType.APPLICATION_JSON)
  public Uni<Avatar> clients(@PathParam("clientId") String clientId) {
    return getComposer().onItem().transformToUni(composer -> composer.getStoreDump());
  }
  
  */
}
