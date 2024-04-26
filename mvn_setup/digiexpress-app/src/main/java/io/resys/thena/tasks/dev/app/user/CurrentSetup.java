package io.resys.thena.tasks.dev.app.user;

import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableChangePrincipalName;
import io.resys.permission.client.api.model.ImmutableCreatePermission;
import io.resys.permission.client.api.model.ImmutableCreatePrincipal;
import io.resys.permission.client.api.model.ImmutableCreateRole;
import io.resys.permission.client.api.model.Principal;
import io.resys.thena.projects.client.api.ImmutableCreateTenantConfig;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.TenantConfig;
import io.resys.thena.projects.client.api.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.dev.app.security.BuiltInDataPermissions;
import io.resys.thena.tasks.dev.app.security.BuiltInRoles;
import io.resys.thena.tasks.dev.app.security.BuiltInUIPermissions;
import io.resys.thena.tasks.dev.app.security.PrincipalCache;
import io.resys.userprofile.client.api.UserProfileClient;
import io.resys.userprofile.client.api.model.ImmutableUpsertUserProfile;
import io.resys.userprofile.client.api.model.UserProfile;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class CurrentSetup {
  @Inject PermissionClient permissions;
  @Inject ProjectClient tenantClient;
  @Inject CurrentTenant currentTenant;
  @Inject UserProfileClient userProfileClient;
  @Inject PrincipalCache cache;
  
  public Uni<CurrentUserConfig> getOrCreateCurrentUserConfig(CurrentUser currentUser) {
    return getOrCreateTenant()
        .onItem().transformToUni(tenant -> getOrCreateUserProfile(tenant, currentUser)
        .onItem().transformToUni(profile -> getOrCreateCurrentUserConfig(profile, tenant, currentUser)));
  }
  
  private Uni<CurrentUserConfig> getOrCreateCurrentUserConfig(UserProfile profile, TenantConfig tenant, CurrentUser currentUser) {
    return getOrCreatePermissions(profile, tenant, currentUser)
        .onItem().transform(permissions -> 
          ImmutableCurrentUserConfig.builder()
          .profile(profile)
          .tenant(tenant)
          .permissions(permissions)
          .build()
        );
  }
  
  private Uni<CurrentPermissions> getOrCreatePermissions(UserProfile profile, TenantConfig tenant, CurrentUser currentUser) {
    final var client = getPermissionsClient(tenant);
    return createRoles(tenant)
      .onItem().transformToUni(junk -> client.roleQuery().get(BuiltInRoles.LOBBY.name()))
      .onItem().transformToUni(lobby -> {
        return client.principalQuery().get(profile.getDetails().getUsername())
            .onFailure().recoverWithUni(e -> createOrLinkPrincipal(tenant, profile, currentUser));
      })
    .onItem().transformToUni(principle -> invalidateCache(principle))
    .onItem().transformToUni(principle -> 
      cache.getPrincipalPermissions(principle.getId(), principle.getEmail())
      .onItem().transform(permissions -> ImmutableCurrentPermissions.builder().addAllPermissions(permissions.getPermissions()).build())
    );
  }
  
  private Uni<Principal> createOrLinkPrincipal(
      TenantConfig tenant, 
      UserProfile profile,
      CurrentUser currentUser) {
    
    final var client = getPermissionsClient(tenant);
    return client.principalQuery().findAllPrincipals()
    .onItem().transformToUni(allPrincipals -> {
      
      final var found = allPrincipals.stream()
        .filter(p -> p.getEmail().equals(profile.getDetails().getEmail()))
        .toList();
      
      // merge principal by email
      if(found.size() == 1) {
        log.warn("App setup, principle for user email: '{}' does not exists, but there is predefined principle with the email, updating principal to match the user!");
        
        client.updatePrincipal().updateOne(ImmutableChangePrincipalName.builder()
            .name(profile.getId())
            .id(found.iterator().next().getId())
            .comment("upsert principal by email from log in")
            .build());
      }
      
      log.warn("App setup, principle for user email: '{}' does not exists, trying to create it!");
      final var createPrincipal = ImmutableCreatePrincipal.builder()
          .name(profile.getId())
          .email(profile.getDetails().getEmail())
          .externalId(currentUser.getUserId())
          .comment("created by default on first user registration")
          .addRoles(BuiltInRoles.LOBBY.name())
          .build();
      return client.createPrincipal().createOne(createPrincipal);
    });
  }
  
  private Uni<Void> createRoles(TenantConfig tenant) {
    final var client = getPermissionsClient(tenant);
    return Multi.createFrom().items(BuiltInRoles.values())
        .onItem().transformToUni(role -> {
          return  client.roleQuery().get(role.name())
            .onFailure().recoverWithUni(e -> {
              log.warn("App setup, system role: '{}' not defined, trying to create it!", role.name());
              return client.createRole().createOne(ImmutableCreateRole.builder()
                  .comment("created by default on first user registration")
                  .name(role.name())
                  .description(role.getDescription())
                  .build());
            });
        })
        .concatenate().collect().asList()
        .onItem().transformToUni(junk -> Uni.createFrom().voidItem());
  }
  
  private Uni<TenantConfig> getOrCreateTenant() {
    return tenantClient.query().repoName(currentTenant.tenantsStoreId(), TenantRepoConfigType.TENANT).get(currentTenant.tenantId())
      .onItem().transformToUni(config -> {
        if(config.isPresent()) {
          return Uni.createFrom().item(config.get());
        }
        return createTenant()
              .onItem().transformToUni(tenantConfig -> createPermissions(tenantConfig)
              .onItem().transform(e -> tenantConfig));
      });
  }
  
  
  private Uni<TenantConfig> createTenant() {
    return tenantClient.query().repoName(currentTenant.tenantsStoreId(), TenantRepoConfigType.TENANT).createIfNot()
    .onItem().transformToUni(created -> tenantClient.createTenantConfig().createOne(ImmutableCreateTenantConfig.builder()
        .repoId(currentTenant.tenantsStoreId())
        .name(currentTenant.tenantId())
        .build()))
    .onItem().transformToUni(tenantConfig -> {
      return Multi.createFrom().items(tenantConfig.getRepoConfigs().stream())
        .onItem().transformToUni(child -> tenantClient.query().repoName(child.getRepoId(), child.getRepoType()).createIfNot())
        .concatenate().collect().asList()
        .onItem().transform(junk -> tenantConfig);
    });
  }
  
  private Uni<Void> createPermissions(TenantConfig tenantConfig) {
    final var permissionClient = getPermissionsClient(tenantConfig);
    return Multi.createFrom().items(BuiltInDataPermissions.values())
      .onItem().transformToUni(permission -> {
        // default data permissions
        final var command = ImmutableCreatePermission.builder()
            .name(permission.name())
            .description(permission.getDescription())
            .comment("created by default")
            .build();
        return permissionClient.createPermission().createOne(command);
      })
      .concatenate().collect().asList()
      .onItem().transformToMulti(junk_ -> Multi.createFrom().items(BuiltInUIPermissions.values()))
      .onItem().transformToUni(permission -> {
        // default ui permissions
        final var command = ImmutableCreatePermission.builder()
            .name(permission.name())
            .description(permission.getDescription())
            .comment("created by default")
            .build();
        return permissionClient.createPermission().createOne(command);
      }).concatenate().collect().asList().onItem().transformToUni(e -> Uni.createFrom().voidItem());
    
  }
  
  private Uni<UserProfile> getOrCreateUserProfile(TenantConfig config, CurrentUser currentUser) {
    final var userProfileConfig = config.getRepoConfig(TenantRepoConfigType.USER_PROFILE);
    final var command = ImmutableUpsertUserProfile.builder()
        .id(currentUser.getUserId())
        .username(currentUser.getUserId())
        .firstName(currentUser.getGivenName())
        .lastName(currentUser.getFamilyName())
        .email(currentUser.getEmail())
        .build();
    return userProfileClient.withRepoId(userProfileConfig.getRepoId()).createUserProfile().createOne(command);
  }
  

  private PermissionClient getPermissionsClient(TenantConfig config) {
    return permissions.withRepoId(config.getRepoConfig(TenantRepoConfigType.PERMISSIONS).getRepoId());
  }

  private <T> Uni<T> invalidateCache(T data) {
    return cache.invalidate()
        .onFailure().transform(e -> {
          log.error("Failed to flush the cache for permissions, {}", e.getMessage(), e);
          return e;
        })
        .onItem().transform(junk -> data); 
  }
}
