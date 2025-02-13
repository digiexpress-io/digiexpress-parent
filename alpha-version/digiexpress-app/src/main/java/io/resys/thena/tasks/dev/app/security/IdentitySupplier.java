package io.resys.thena.tasks.dev.app.security;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ChangeType;
import io.resys.permission.client.api.model.ImmutableChangePrincipalExternalId;
import io.resys.permission.client.api.model.ImmutableChangePrincipalName;
import io.resys.permission.client.api.model.ImmutableChangePrincipalRoles;
import io.resys.permission.client.api.model.ImmutableCreatePrincipal;
import io.resys.permission.client.api.model.ImmutablePrincipal;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.permission.client.api.model.PrincipalCommand.PrincipalUpdateCommand;
import io.resys.thena.api.entities.org.OrgActorStatusType;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.TenantConfig;
import io.resys.thena.projects.client.api.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.TenantConfig.TenantRepoConfigType;
import io.resys.thena.support.RepoAssert;
import io.resys.thena.tasks.dev.app.events.EventPolicy;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.resys.thena.tasks.dev.app.user.CurrentUserConfig;
import io.resys.thena.tasks.dev.app.user.ImmutableCurrentPermissions;
import io.resys.thena.tasks.dev.app.user.ImmutableCurrentUserConfig;
import io.resys.userprofile.client.api.UserProfileClient;
import io.resys.userprofile.client.api.UserProfileClient.UserProfileNotFoundException;
import io.resys.userprofile.client.api.model.ImmutableUpsertUserProfile;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Singleton
public class IdentitySupplier {
  private static final String CACHE_NAME = "PRINCIPAL_CACHE";
  @Inject private CurrentTenant currentTenant;
  @Inject private ProjectClient tenantClient;  
  @Inject private PermissionClient permissions;
  @Inject private UserProfileClient userProfileClient;
  @Inject private EventPolicy events;

  @ConfigProperty(name = "tenant.failSafeUsers")
  String failSafeUsers;

  @CacheResult(cacheName = IdentitySupplier.CACHE_NAME)
  public Uni<Map<String, Role>> getRolePermissions() {
    return getClient().onItem().transformToUni(client -> client.roleQuery().findAllRoles())
        .onItem().transform(roles -> {
          
          final var builder = ImmutableMap.<String, Role>builder();
          
          roles.forEach(role -> {
            builder.put(role.getId(), role);
            builder.put(role.getName(), role);
          });
          
          return builder.build();
        });
  }
  
  @CacheResult(cacheName = IdentitySupplier.CACHE_NAME)
  public Uni<Principal> getPrincipalPermissions(String principalId, String email) {
    return getClient().onItem()
        .transformToUni(client -> {
          return client.principalQuery().get(principalId)
          .onFailure()
          .recoverWithUni(e -> {
            log.warn("Failed to find principal by id: {}, trying to find by email: {}, error: {}", principalId, email, e.getMessage(), e);
            return client.principalQuery().get(email);
          });
        }).onItem().transform(principal -> {
          if(isFailSafeUser(principalId, email)) {
            final var failSafe = Arrays.asList(BuiltInDataPermissions.values()).stream().map(e -> e.name()).toList();
            return ImmutablePrincipal.builder().from(principal).addAllPermissions(failSafe).build();
          }
          return principal;
        })
        .onFailure().recoverWithItem(() -> onFailureRecoverWith(principalId, email));
  }
  
  
  @CacheInvalidateAll(cacheName = IdentitySupplier.CACHE_NAME)
  public Uni<Void> invalidate() {
    return Uni.createFrom().voidItem()
        .onItem().transformToUni(junk -> events.sendAmUpdate());
  }  
  
  @CacheResult(cacheName = IdentitySupplier.CACHE_NAME)
  public Uni<CurrentUserConfig> getOrCreateCurrentUserConfig(CurrentUser currentUser) {
    return tenantClient.query().repoName(currentTenant.tenantsStoreId(), TenantRepoConfigType.TENANT).get(currentTenant.tenantId())
    .onItem().transformToUni(tenantConfig -> {
      RepoAssert.isTrue(tenantConfig.isPresent(), () -> "Tenant must created!");

      final var client = getPermissionsClient(tenantConfig.get());

      return client.principalQuery().get(currentUser.getUserId())
          .onFailure().recoverWithUni(e -> upsertPrincipal(tenantConfig.get(), currentUser))
          .onItem().transformToUni(principal -> upsertUserConfig(tenantConfig.get(), principal, currentUser));
    })
    ;
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
    
    final var permissionUni = getPrincipalPermissions(principal.getId(), principal.getEmail());
    final var profileUni = client.userProfileQuery().get(principal.getId())
        .onFailure(UserProfileNotFoundException.class)
        .recoverWithUni((exception) -> {
          final var command = ImmutableUpsertUserProfile.builder()
              .id(principal.getId())
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
    return invalidate()
        .onFailure().transform(e -> {
          log.error("Failed to flush the cache for permissions, {}", e.getMessage(), e);
          return e;
        })
        .onItem().transform(junk -> data); 
  }
  
  private Uni<PermissionClient> getClient() {
    return getPermissionConfig().onItem().transform(config -> permissions.withRepoId(config.getRepoId()));
  }
  private Uni<TenantRepoConfig> getPermissionConfig() {
    return tenantClient.queryActiveTenantConfig().get(currentTenant.tenantId())
      .onItem().transform(config -> config.getRepoConfig(TenantRepoConfigType.PERMISSIONS));
  }
  
  private boolean isFailSafeUser(String principalId, String email) {
    return Arrays.asList(this.failSafeUsers.split(";")).stream()
        .map(e -> e.trim().toLowerCase())
        .filter(e -> e.equals(principalId.toLowerCase()) || e.equals(email.toLowerCase()))
        .count() > 0;
  }
  

  private Principal onFailureRecoverWith(String principalId, String email) {
    if(isFailSafeUser(principalId, email)) {
      final var failSafe = Arrays.asList(BuiltInDataPermissions.values()).stream().map(e -> e.name()).toList();
      return ImmutablePrincipal.builder()
          .id(principalId)
          .version("")
          .email(email)
          .status(OrgActorStatusType.IN_FORCE)
          .name(principalId)
          .addAllPermissions(failSafe).build();
    }
    
    return ImmutablePrincipal.builder()
        .id(principalId)
        .version("")
        .email(email)
        .status(OrgActorStatusType.IN_FORCE)
        .name(principalId)
        .build();
  }
}
