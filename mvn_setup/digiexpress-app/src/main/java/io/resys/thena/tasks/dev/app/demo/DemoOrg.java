package io.resys.thena.tasks.dev.app.demo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableCreatePermission;
import io.resys.permission.client.api.model.ImmutableCreatePrincipal;
import io.resys.permission.client.api.model.ImmutableCreateRole;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.dev.app.security.BuiltInRoles;
import io.resys.thena.tasks.dev.app.security.PrincipalCache;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.resys.userprofile.client.api.UserProfileClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ApplicationScoped
public class DemoOrg {
  @Inject PermissionClient permissions;
  @Inject TenantConfigClient tenantClient;
  @Inject CurrentTenant currentTenant;
  @Inject UserProfileClient userProfileClient;
  @Inject PrincipalCache cache;
  
  public static final Map<String, Integer> ASSIGNEES = Map.of(
    "olev.mutso@resys.io", 1,
    "jocelyn.mutso@resys.io", 2,
    "mika.lindholm@resys.io", 3,
    "vahur.krouverk@resys.io", 4,
    "mika.argillander@resys.io", 5
  ); 

  private static final String taskAdmin = "task-admin-role";
  public static final Map<String, Integer> ROLES = Map.of(
      taskAdmin, 1,
      "water-department", 2,
      "education-department", 3,
      "elderly-care-department", 4,
      "sanitization-department", 5
      ); 
  
  public Uni<Void> generate() {
    return tenantClient.query()
        .repoName(currentTenant.tenantsStoreId(), TenantRepoConfigType.TENANT)
        .get(currentTenant.tenantId())
        .onItem().transformToUni(config -> {
          return Uni.combine().all().unis(
              createPrincipals(config.get()),
              createRoles(config.get())
          ).asTuple();
        })
        .onItem().transformToUni(junk -> Uni.createFrom().voidItem());
  }
  
  private PermissionClient getPermissionsClient(TenantConfig config) {
    return permissions.withRepoId(config.getRepoConfig(TenantRepoConfigType.PERMISSIONS).getRepoId());
  }
  private Uni<Map<Integer, Principal>> createPrincipals(TenantConfig tenant) {
    final var client = getPermissionsClient(tenant);
    return Multi.createFrom().items(ASSIGNEES.keySet().stream())
        .onItem().transformToUni(email -> {
          log.warn("Demo setup, creating principle for user email: '{}'!", email);
          return client.createPrincipal().createOne(ImmutableCreatePrincipal.builder()
              .name(email)
              .email(email)
              .externalId(email)
              .comment("Created by demo")
              .addRoles(BuiltInRoles.LOBBY.name())
              .addRoles(BuiltInRoles.TASK_WORKER.name())
              .build());
        })
        .concatenate().collect().asList()
        .onItem().transform(principals -> principals.stream().collect(Collectors.toMap(e -> ASSIGNEES.get(e.getEmail()), e -> e)));
  }
  
  private Uni<Map<Integer, Role>> createRoles(TenantConfig tenant) {
    final var client = getPermissionsClient(tenant);
    return Multi.createFrom().items(
          ROLES.keySet().stream().filter(e -> !e.equals(taskAdmin))
        ).onItem().transformToUni(roleName -> {
          log.warn("Demo setup, creating system role: '{}'!", roleName);
          final var createRole = ImmutableCreateRole.builder()
              .parentId(BuiltInRoles.TASK_WORKER.name())
              .comment("created by default on first user registration")
              .name(roleName)
              .description("Demo role")
              .build();
          
          return client.createRole().createOne(createRole)
              .onItem()
              .transformToUni(role -> {
                final var createPermission = ImmutableCreatePermission.builder()
                    .name("TASK_CRUD_"+ role.getName())
                    .description("demo permission")
                    .comment("Created by demo")
                    .addRoles(role.getId())
                    .build();
                return client.createPermission().createOne(createPermission).onItem().transform(junk -> role);
              });
        })
        .concatenate()
        .collect().asList()
        .onItem().transformToUni(roles -> {
          // create admin
          return client.permissionQuery().findAllPermissions()
          .onItem().transformToUni(permissions -> {
            log.warn("Demo setup, creating system role: '{}'!", taskAdmin);
            final var createRole = ImmutableCreateRole.builder()
                .comment("Created by demo")
                .name(taskAdmin)
                .description("Demo role")
                .addAllPermissions(permissions.stream()
                    .filter(e -> e.getName().startsWith("TASK_CRUD_"))
                    .map(e -> e.getName())
                    .toList())
                .build();
            return client.createRole().createOne(createRole);
          }).onItem().transform(adminRole -> ImmutableList.<Role>builder()
              .add(adminRole)
              .addAll(roles)
              .build());
        })
        .onItem().transform(roles -> roles.stream().collect(Collectors.toMap(e -> ROLES.get(e.getName()), e -> e)));
  }
  
  public static String getAssignee(int index, Tuple2<List<Principal>, List<Role>> permissions) {
     
    for(final var user : permissions.getItem1()) {
      if(!ASSIGNEES.containsKey(user.getEmail())) {
        continue;
      }
       
      if(ASSIGNEES.get(user.getEmail()).equals(index)) {
        return user.getId();
      }
    }
    
    return null;
    
  }
  
  public static String getRole(int index, Tuple2<List<Principal>, List<Role>> permissions) {
    return permissions.getItem2().stream().filter(e -> {
      if(!ROLES.containsKey(e.getName())) {
        return false;
      }
      return ROLES.get(e.getName()) == index;
    }).findFirst().get().getId();
  }
}
