package io.resys.thena.tasks.dev.app;

import java.util.List;

import io.resys.avatar.client.api.Avatar;
import io.resys.avatar.client.api.AvatarClient;
import io.resys.avatar.client.api.AvatarNotFoundException;
import io.resys.avatar.client.api.AvatarRestApi;
import io.resys.avatar.client.api.ImmutableCreateAvatar;
import io.resys.crm.client.api.CrmClient;
import io.resys.crm.client.api.CrmClient.CustomerNotFoundException;
import io.resys.crm.client.api.model.Customer;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.PermissionClient.RoleNotFoundException;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.dev.app.security.PrincipalCache;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.resys.userprofile.client.api.UserProfileClient;
import io.resys.userprofile.client.api.UserProfileClient.UserProfileNotFoundException;
import io.resys.userprofile.client.api.model.UserProfile;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple4;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;



@Path("q/digiexpress/api/avatars")
public class AvatarResource implements AvatarRestApi {

  @Inject PermissionClient permissions;
  @Inject CrmClient crmClient;
  @Inject CurrentTenant currentTenant;
  @Inject CurrentUser currentUser;
  @Inject ProjectClient tenantClient;
  @Inject UserProfileClient profileClient;
  @Inject AvatarClient avatarClient;
  @Inject PrincipalCache cache;
  
  @Override
  public Uni<io.resys.avatar.client.api.Avatar> getOrCreateAvatar(String userId) {
    return getAvatarClient().onItem().transformToUni(clients -> 
      clients.getItem1().queryAvatars().get(userId)
      .onFailure(AvatarNotFoundException.class)
      
      .recoverWithUni((t) -> clients.getItem2().userProfileQuery().get(userId).onItem().transformToUni(this::createAvatar))
      .onFailure(UserProfileNotFoundException.class)
      
      .recoverWithUni((t) -> clients.getItem4().roleQuery().get(userId).onItem().transformToUni(this::createAvatar))
      .onFailure(RoleNotFoundException.class)
      
      .recoverWithUni((t) -> clients.getItem3().customerQuery().get(userId).onItem().transformToUni(this::createAvatar))
      .onFailure(CustomerNotFoundException.class)
      .transform((t) -> new AvatarNotFoundException("Avatar not found by id: '" + userId + "'!"))
    );
  }

  @Override
  public Uni<List<io.resys.avatar.client.api.Avatar>> findAllAvatars() {
    // TODO Auto-generated method stub
    return null;
  }
  private Uni<Tuple4<AvatarClient, UserProfileClient, CrmClient, PermissionClient>> getAvatarClient() {
    return tenantClient.queryActiveTenantConfig().get(currentTenant.tenantId()) 
        .onItem().transform(config -> {
          return Tuple4.of(
              avatarClient.withTenantId(config.getRepoConfig(TenantRepoConfigType.AVATARS).getRepoId()), 
              profileClient.withRepoId(config.getRepoConfig(TenantRepoConfigType.USER_PROFILE).getRepoId()), 
              crmClient.withRepoId(config.getRepoConfig(TenantRepoConfigType.CRM).getRepoId()), 
              permissions.withRepoId(config.getRepoConfig(TenantRepoConfigType.PERMISSIONS).getRepoId()));
        });
  }
  
  private Uni<Avatar> createAvatar(UserProfile profile) {
    return getAvatarClient().onItem().transformToUni(tuple -> 
      tuple.getItem1().createAvatar()
        .createOne(ImmutableCreateAvatar.builder()
          .avatarType("PROFILE")
          .id(profile.getId())
          .seedData(profile.getDetails().getEmail())
          .externalId(profile.getId())
        .build())
    );
  } 
  private Uni<Avatar> createAvatar(Role profile) {
    return getAvatarClient().onItem().transformToUni(tuple -> 
    tuple.getItem1().createAvatar()
      .createOne(ImmutableCreateAvatar.builder()
          .avatarType("ROLE")
          .id(profile.getId())
          .seedData(profile.getName())
          .externalId(profile.getName())
          .avatarType("system_user")
        .build()
      )
  );
  }
  private Uni<Avatar> createAvatar(Customer profile) {
    return getAvatarClient().onItem().transformToUni(tuple -> 
    tuple.getItem1().createAvatar()
      .createOne(ImmutableCreateAvatar.builder()
        .avatarType("CUSTOMER")
        .id(profile.getId())
        .seedData(profile.getBody().getContact().isPresent() ? 
          profile.getBody().getContact().map(contact -> contact.getEmail()).get()
          : 
          profile.getBody().getUsername())
        .externalId(profile.getId())
      .build())
  );
  }


}
