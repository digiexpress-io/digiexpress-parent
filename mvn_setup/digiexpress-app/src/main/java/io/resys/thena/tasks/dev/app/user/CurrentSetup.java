package io.resys.thena.tasks.dev.app.user;

import java.time.Instant;

import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableCreatePrincipal;
import io.resys.permission.client.api.model.ImmutableCreateRole;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.ImmutableCreateTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.dev.app.security.BuiltInRoles;
import io.resys.thena.tasks.dev.app.security.PrincipalCache;
import io.resys.userprofile.client.api.UserProfileClient;
import io.resys.userprofile.client.api.model.ImmutableUpsertUserProfile;
import io.resys.userprofile.client.api.model.ImmutableUserDetails;
import io.resys.userprofile.client.api.model.UserProfile;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestScoped
public class CurrentSetup {
  @Inject PermissionClient permissions;
  @Inject TenantConfigClient tenantClient;
  @Inject CurrentTenant currentTenant;
  @Inject CurrentUser currentUser;
  @Inject UserProfileClient userProfileClient;
  @Inject PrincipalCache cache;
  
  public Uni<CurrentUserConfig> getOrCreateCurrentUserConfig() {
    return getOrCreateTenant()
        .onItem().transformToUni(tenant -> getOrCreateUserProfile(tenant)
        .onItem().transformToUni(profile -> getOrCreateCurrentUserConfig(profile, tenant)));
  }
  
  private Uni<CurrentUserConfig> getOrCreateCurrentUserConfig(UserProfile profile, TenantConfig tenant) {
    return getOrCreatePermissions(profile, tenant)
        .onItem().transform(permissions -> 
          ImmutableCurrentUserConfig.builder()
          .profile(profile)
          .tenant(currentTenant)
          .permissions(permissions)
          .build()
        );
  }
  
  private Uni<CurrentPermissions> getOrCreatePermissions(UserProfile profile, TenantConfig tenant) {
    final var client = getPermissionsClient(tenant);
    return client.roleQuery().get(BuiltInRoles.LOBBY.name())
    .onFailure().recoverWithUni(e -> {
      log.warn("App setup, system role: 'LOBBY' not defined, trying to create it!");
      return client.createRole().createOne(ImmutableCreateRole.builder()
          .comment("created by default on first user registration")
          .name(BuiltInRoles.LOBBY.name())
          .description(BuiltInRoles.LOBBY.getDescription())
          .build());
    })
    .onItem().transformToUni(lobby -> {
      return client.principalQuery().get(profile.getDetails().getUsername())
          .onFailure().recoverWithUni(e -> {
            log.warn("App setup, principle for user email: '{}' does not exists, trying to create it!");
            return client.createPrincipal().createOne(ImmutableCreatePrincipal.builder()
                .name(profile.getId())
                .email(profile.getDetails().getEmail())
                .externalId(currentUser.getUserId())
                .comment("created by default on first user registration")
                .addRoles(lobby.getName())
                .build());
          });
    })
    .onItem().transformToUni(principle -> invalidateCache(principle))
    .onItem().transformToUni(principle -> 
      cache.getPrincipalPermissions(principle.getId(), principle.getEmail())
      .onItem().transform(permissions -> ImmutableCurrentPermissions.builder().addAllPermissions(permissions).build())
    );
  }
  
  private Uni<TenantConfig> getOrCreateTenant() {
    return tenantClient.query().repoName(currentTenant.tenantsStoreId(), TenantRepoConfigType.TENANT).get(currentTenant.tenantId())
      .onItem().transformToUni(config -> {
        if(config.isEmpty()) {
          return tenantClient.query().repoName(currentTenant.tenantsStoreId(), TenantRepoConfigType.TENANT).createIfNot()
              .onItem().transformToUni(created -> tenantClient.createTenantConfig().createOne(ImmutableCreateTenantConfig.builder()
                  .repoId(currentTenant.tenantsStoreId())
                  .name(currentTenant.tenantId())
                  .targetDate(Instant.now())
                  .build()))
              .onItem().transformToUni(tenantConfig -> {
                return Multi.createFrom().items(tenantConfig.getRepoConfigs().stream())
                  .onItem().transformToUni(child -> tenantClient.query().repoName(child.getRepoId(), child.getRepoType()).createIfNot())
                  .concatenate().collect().asList()
                  .onItem().transform(junk -> tenantConfig);
              });
        }
        return Uni.createFrom().item(config.get());
      });
  }
  
  private Uni<UserProfile> getOrCreateUserProfile(TenantConfig config) {
    final var userProfileConfig = config.getRepoConfigs().stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.USER_PROFILE).findFirst().get();
    return userProfileClient.withRepoId(userProfileConfig.getRepoId()).createUserProfile().createOne(ImmutableUpsertUserProfile.builder()
      .userId(currentUser.getUserId())
      .targetDate(Instant.now())
      .id(currentUser.getUserId())
      .details(ImmutableUserDetails.builder()
          .username(currentUser.getUserId())
          .firstName(currentUser.getGivenName())
          .lastName(currentUser.getFamilyName())
          .email(currentUser.getEmail())
          .build())
      .build());
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
