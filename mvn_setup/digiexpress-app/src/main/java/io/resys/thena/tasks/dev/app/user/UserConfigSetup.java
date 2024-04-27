package io.resys.thena.tasks.dev.app.user;

import java.util.List;

import com.google.common.collect.ImmutableList;

import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ChangeType;
import io.resys.permission.client.api.model.ImmutableChangePrincipalExternalId;
import io.resys.permission.client.api.model.ImmutableChangePrincipalName;
import io.resys.permission.client.api.model.ImmutableChangePrincipalRoles;
import io.resys.permission.client.api.model.ImmutableCreatePrincipal;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.PrincipalCommand.PrincipalUpdateCommand;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.TenantConfig;
import io.resys.thena.projects.client.api.TenantConfig.TenantRepoConfigType;
import io.resys.thena.support.RepoAssert;
import io.resys.thena.tasks.dev.app.security.BuiltInRoles;
import io.resys.thena.tasks.dev.app.security.PrincipalCache;
import io.resys.userprofile.client.api.UserProfileClient;
import io.resys.userprofile.client.api.UserProfileClient.UserProfileNotFoundException;
import io.resys.userprofile.client.api.model.ImmutableUpsertUserProfile;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class UserConfigSetup {
  @Inject PermissionClient permissions;
  @Inject ProjectClient tenantClient;
  @Inject CurrentTenant currentTenant;
  @Inject UserProfileClient userProfileClient;
  @Inject PrincipalCache cache;
  
  public Uni<CurrentUserConfig> getOrCreateCurrentUserConfig(CurrentUser currentUser) {
    return tenantClient.query().repoName(currentTenant.tenantsStoreId(), TenantRepoConfigType.TENANT).get(currentTenant.tenantId())
    .onItem().transformToUni(tenantConfig -> {
      RepoAssert.isTrue(tenantConfig.isPresent(), () -> "Tenant must created!");

      final var client = getPermissionsClient(tenantConfig.get());

      return client.principalQuery().get(currentUser.getUserId())
          .onFailure().recoverWithUni(e -> upsertPrincipal(tenantConfig.get(), currentUser))
          .onItem().transformToUni(principal -> upsertUserConfig(tenantConfig.get(), principal, currentUser));
    });
  }
  
  
  private Uni<Principal> upsertPrincipal(TenantConfig tenant, CurrentUser currentUser) {
    
    final var client = getPermissionsClient(tenant);
    return client.principalQuery().findAllPrincipals().onItem().transformToUni(allPrincipals -> {
      
      final var found = allPrincipals.stream()
        .filter(p -> p.getEmail().equals(currentUser.getEmail()))
        .toList();
      
      // merge principal by email
      if(found.size() == 1) {
        log.warn("App setup, principle for user email: '{}' does not exists, but there is predefined principle with the email, updating principal to match the user!");
        
        final List<PrincipalUpdateCommand> commands = ImmutableList.<PrincipalUpdateCommand>builder()
            .add(ImmutableChangePrincipalName.builder()
            .name(currentUser.getEmail() + "_" + currentUser.getUserId())
            .id(found.iterator().next().getId())
            .comment("upsert principal by email from log in")
            .build())
            
            .add(ImmutableChangePrincipalExternalId.builder()
            .externalId(currentUser.getUserId())
            .id(found.iterator().next().getId())
            .comment("upsert principal by email from log in")
            .build())
            
            .add(ImmutableChangePrincipalRoles.builder()
            .changeType(ChangeType.ADD)
            .addRoles(BuiltInRoles.LOBBY.name())
            .id(found.iterator().next().getId())
            .comment("upsert principal by email from log in")
            .build())
            
            .build();

        return client.updatePrincipal().updateOne(commands);
        
      }
      
      log.warn("App setup, principle for user email: '{}' does not exists, trying to create it!", currentUser.getEmail());
      final var createPrincipal = ImmutableCreatePrincipal.builder()
          .name(currentUser.getUserId())
          .email(currentUser.email())
          .externalId(currentUser.getUserId())
          .comment("created by default on first user registration")
          .addRoles(BuiltInRoles.LOBBY.name())
          .build();
      return client.createPrincipal().createOne(createPrincipal);
    })
    .onItem().transformToUni((Principal e) -> invalidateCache(e));
  }

   
  
  private Uni<CurrentUserConfig> upsertUserConfig(TenantConfig config, Principal principal, CurrentUser currentUser) {
    final var userProfileConfig = config.getRepoConfig(TenantRepoConfigType.USER_PROFILE);
    final var client = userProfileClient.withRepoId(userProfileConfig.getRepoId());
    
    final var permissionUni = cache.getPrincipalPermissions(principal.getId(), principal.getEmail());
    final var profileUni = client.userProfileQuery().get(principal.getId())
        .onFailure(UserProfileNotFoundException.class)
        .recoverWithUni((exception) -> {
          final var command = ImmutableUpsertUserProfile.builder()
              .id(currentUser.getUserId())
              .username(currentUser.getUserId())
              .firstName(currentUser.getGivenName())
              .lastName(currentUser.getFamilyName())
              .email(currentUser.getEmail())
              .build();
          return client.createUserProfile().createOne(command);
        });
    
    
    return Uni.combine().all().unis(permissionUni, profileUni).asTuple()
        .onItem().transform(tuple -> {
          
          final var permissions = ImmutableCurrentPermissions.builder()
            .principal(principal)
            .addAllPermissions(tuple.getItem1().getPermissions())
            .build();
        
          return ImmutableCurrentUserConfig.builder()
            .profile(tuple.getItem2())
            .tenant(config)
            .permissions(permissions)
            .build();
        });
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
