package io.resys.thena.tasks.dev.app;

import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

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
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.tuples.Tuple4;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;


@Singleton
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
    return getAvatarClient().onItem().transformToUni(clients -> clients.getItem1().queryAvatars().get(userId)
      
      .onFailure(AvatarNotFoundException.class)
      .recoverWithUni((t) -> clients.getItem2().userProfileQuery().get(userId).onItem().transformToUni(data ->
        clients.getItem1().createAvatar().createOne(createAvatar(data))))
      
      .onFailure(UserProfileNotFoundException.class)
      .recoverWithUni((t) -> clients.getItem4().roleQuery().get(userId).onItem().transformToUni(data -> 
        clients.getItem1().createAvatar().createOne(createAvatar(data))))

      .onFailure(RoleNotFoundException.class)
      .recoverWithUni((t) -> clients.getItem3().customerQuery().get(userId).onItem().transformToUni(data -> 
        clients.getItem1().createAvatar().createOne(createAvatar(data))))
      
      .onFailure(CustomerNotFoundException.class)
      .transform((t) -> new AvatarNotFoundException("Avatar not found by id: '" + userId + "'!"))
    );
  }
  
  @Override
  public Uni<List<Avatar>> getOrCreateAvatars(GetOrCreateAvatars request) {
    return getAvatarClient().onItem()
        .transformToUni(clients -> getOrCreateAvatars(clients, request));
  }
  
  public Uni<List<Avatar>> getOrCreateAvatars(Tuple4<AvatarClient, UserProfileClient, CrmClient, PermissionClient> clients, GetOrCreateAvatars request) {
    final Stream<Uni<Tuple2<Avatar, ImmutableCreateAvatar>>> stream = request.getId().stream().map(id -> create(clients, id));
    
    
    return Multi.createFrom().items(stream)
        .onItem().transformToUni(x -> x).concatenate().collect().asList()
        .onItem().transformToUni(requests -> {
          
          final var avatars = requests.stream().filter(e -> e.getItem1() != null).map(e -> e.getItem1()).toList();
          final List<ImmutableCreateAvatar> commands = requests.stream().filter(e -> e.getItem2() != null).map(e -> e.getItem2()).toList();
          
          return clients.getItem1().createAvatar().createMany(commands)
              .onItem().transform(created -> ImmutableList.<Avatar>builder()
                  .addAll(created)
                  .addAll(avatars)
                  .build());
        });

  }

  private Uni<Tuple2<Avatar, ImmutableCreateAvatar>> create(Tuple4<AvatarClient, UserProfileClient, CrmClient, PermissionClient> clients, String userId) {
    return clients.getItem1().queryAvatars().get(userId)
    .onItem().transform(avatar -> Tuple2.<Avatar, ImmutableCreateAvatar>of(avatar, null))
    .onFailure(AvatarNotFoundException.class)
    .recoverWithUni((t) -> clients.getItem2().userProfileQuery().get(userId)
        .onItem().transform(this::createAvatar)
        .onItem().transform(data -> Tuple2.<Avatar, ImmutableCreateAvatar>of(null, data)))
   
    .onFailure(UserProfileNotFoundException.class)
    .recoverWithUni((t) -> clients.getItem4().roleQuery().get(userId)
        .onItem().transform(this::createAvatar)
        .onItem().transform(data -> Tuple2.<Avatar, ImmutableCreateAvatar>of(null, data)))


    .onFailure(RoleNotFoundException.class)
    .recoverWithUni((t) ->  
      clients.getItem3().customerQuery().get(userId)
      .onItem().transform(this::createAvatar)
      .onItem().transform(data -> Tuple2.<Avatar, ImmutableCreateAvatar>of(null, data))
      .onFailure(CustomerNotFoundException.class).transform((junk) -> new AvatarNotFoundException("Avatar not found by id: '" + userId + "'!"))
     );
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
  
  private ImmutableCreateAvatar createAvatar(UserProfile profile) {
    return ImmutableCreateAvatar.builder()
          .avatarType("PROFILE")
          .id(profile.getId())
          .seedData(profile.getDetails().getEmail())
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
