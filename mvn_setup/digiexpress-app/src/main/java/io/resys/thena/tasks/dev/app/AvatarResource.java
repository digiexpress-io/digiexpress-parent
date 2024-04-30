package io.resys.thena.tasks.dev.app;

import java.util.List;

import com.google.common.collect.ImmutableList;

import io.quarkus.cache.CacheResult;
import io.resys.avatar.client.api.Avatar;
import io.resys.avatar.client.api.AvatarClient;
import io.resys.avatar.client.api.AvatarNotFoundException;
import io.resys.avatar.client.api.AvatarRestApi;
import io.resys.avatar.client.api.ImmutableCreateAvatar;
import io.resys.crm.client.api.CrmClient;
import io.resys.crm.client.api.CrmClient.CustomerNotFoundException;
import io.resys.crm.client.api.model.Customer;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.PermissionClient.PrincipalNotFoundException;
import io.resys.permission.client.api.PermissionClient.RoleNotFoundException;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.dev.app.security.PrincipalCache;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.resys.userprofile.client.api.UserProfileClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple4;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;


@Singleton
@Path("q/digiexpress/api/avatars")
public class AvatarResource implements AvatarRestApi {
  private static final String CACHE_NAME = "AVATAR_CACHE";
  @Inject PermissionClient permissions;
  @Inject CrmClient crmClient;
  @Inject CurrentTenant currentTenant;
  @Inject CurrentUser currentUser;
  @Inject ProjectClient tenantClient;
  @Inject UserProfileClient profileClient;
  @Inject AvatarClient avatarClient;
  @Inject PrincipalCache cache;
  
  @CacheResult(cacheName = AvatarResource.CACHE_NAME)
  @Override
  public Uni<io.resys.avatar.client.api.Avatar> getOrCreateAvatar(String userId) {
    return getAvatarClient().onItem().transformToUni(clients -> clients.getItem1().queryAvatars().get(userId)
      
      .onFailure(AvatarNotFoundException.class)
      .recoverWithUni((t) -> clients.getItem4().principalQuery().get(userId).onItem().transformToUni(data ->
        clients.getItem1().createAvatar().createOne(createAvatar(data))))
      
      .onFailure(PrincipalNotFoundException.class)
      .recoverWithUni((t) -> clients.getItem4().roleQuery().get(userId).onItem().transformToUni(data -> 
        clients.getItem1().createAvatar().createOne(createAvatar(data))))

      .onFailure(RoleNotFoundException.class)
      .recoverWithUni((t) -> clients.getItem3().customerQuery().get(userId).onItem().transformToUni(data -> 
        clients.getItem1().createAvatar().createOne(createAvatar(data))))
      
      .onFailure(CustomerNotFoundException.class)
      .transform((t) -> new AvatarNotFoundException("Avatar can't be created for id: '" + userId + "'!"))
    );
  }
  
  @CacheResult(cacheName = AvatarResource.CACHE_NAME)
  @Override
  public Uni<List<Avatar>> getOrCreateAvatars(GetOrCreateAvatars request) {
    return getAvatarClient().onItem()
        .transformToUni(clients -> getOrCreateAvatars(clients, request));
  }
  
  public Uni<List<Avatar>> getOrCreateAvatars(Tuple4<AvatarClient, UserProfileClient, CrmClient, PermissionClient> clients, GetOrCreateAvatars request) {
    
    return clients.getItem1().queryAvatars().findByIds(request.getId())
        .onItem().transformToUni(found -> {
          if(found.size() == request.getId().size()) {
            return Uni.createFrom().item(found);
          }
          
          final var foundIds = found.stream().map(e -> e.getId()).toList();
          final var requests = request.getId().stream().filter(f -> !foundIds.contains(f)).map(id -> create(clients, id)).toList();
          
          
          return Uni.join().all(requests).andCollectFailures()
              .onItem().transformToUni(commands -> clients.getItem1().createAvatar().createMany(commands)
                  .onItem().transform(created -> ImmutableList.<Avatar>builder()
                      .addAll(created)
                      .addAll(found)
                      .build()));
          
        });
  }

  private Uni<ImmutableCreateAvatar> create(Tuple4<AvatarClient, UserProfileClient, CrmClient, PermissionClient> clients, String userId) {
    return clients.getItem4().principalQuery().get(userId).onItem().transform(this::createAvatar)
   
    .onFailure(PrincipalNotFoundException.class)
    .recoverWithUni((t) -> clients.getItem4().roleQuery().get(userId).onItem().transform(this::createAvatar))

    .onFailure(RoleNotFoundException.class)
    .recoverWithUni((t) -> clients.getItem3().customerQuery().get(userId).onItem().transform(this::createAvatar))
    
    .onFailure(CustomerNotFoundException.class).transform((junk) -> new AvatarNotFoundException("Avatar can't be created for id: '" + userId + "'!"));
  }
  

  @Override
  public Uni<List<io.resys.avatar.client.api.Avatar>> findAllAvatars() {
    return getAvatarClient().onItem().transformToUni(clients -> clients.getItem1().queryAvatars().findAll());
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
  
  private ImmutableCreateAvatar createAvatar(Principal profile) {
    return ImmutableCreateAvatar.builder()
          .avatarType("PRINCIPAL")
          .id(profile.getId())
          .seedData(profile.getEmail())
          .externalId(profile.getId())
        .build();
  } 
  private ImmutableCreateAvatar createAvatar(Role profile) {
    return ImmutableCreateAvatar.builder()
          .avatarType("ROLE")
          .id(profile.getId())
          .seedData(profile.getName())
          .externalId(profile.getName())
          .avatarType("system_user")
        .build();
  }
  private ImmutableCreateAvatar createAvatar(Customer profile) {
    return ImmutableCreateAvatar.builder()
        .avatarType("CUSTOMER")
        .id(profile.getId())
        .seedData(profile.getBody().getContact().isPresent() ? 
          profile.getBody().getContact().map(contact -> contact.getEmail()).get()
          : 
          profile.getBody().getUsername())
        .externalId(profile.getId())
      .build();
  }


}
