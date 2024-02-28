package io.resys.permission.client.rest;

import java.util.List;

import io.resys.permission.client.api.model.PermissionCommand.CreatePermission;
import io.resys.permission.client.api.model.PermissionCommand.PermissionUpdateCommand;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.permission.client.api.model.PrincipalCommand.PrincipalUpdateCommand;
import io.resys.permission.client.api.model.RoleCommand.RoleUpdateCommand;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/*
 * [x] GET all principals
 * [x] GET one principal
 * [x] PUT one principal
 * [ ] PUT many principals
 * 
 * [x] GET all permissions
 * [x] GET one permission
 * [x] POST one permission
 * [x] PUT one permission
 * [ ] PUT many permissions
 * 
 * [x] GET all roles
 * [x] GET one role
 * [x] POST one role
 * [x] PUT one role
 * [ ] PUT many roles
 * 
 */


public interface PermissionRestApi {
  
  @GET @Path("principals") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<Principal>> findAllPrincipals();
  
  @GET @Path("principals/{principalId}") @Produces(MediaType.APPLICATION_JSON)
  Uni<Principal> getPrincipalById(@PathParam("principalId") String principalId);
  
  @PUT @Path("principals/{principalId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Principal> updatePrincipal(@PathParam("principalId") String principalId, List<PrincipalUpdateCommand> commands);
  
  @GET @Path("permissions") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<Permission>> findAllPermissions();
  
  @POST @Path("permissions") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Permission> createPermission(CreatePermission command);
  
  @GET @Path("permissions/{permissionId}") @Produces(MediaType.APPLICATION_JSON)
  Uni<Permission> getPermissionById(@PathParam("permissionId") String permissionId);
  
  @PUT @Path("permissions/{permissionId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Permission> updatePermission(@PathParam("permissionId") String permissionId, List<PermissionUpdateCommand> commands);
  
  @GET @Path("roles") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<Role>> findAllRoles();
  
  @POST @Path("roles") @Produces(MediaType.APPLICATION_JSON)
  Uni<Role> createRole();
  
  @PUT @Path("roles/{roleId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Role> updateRole(@PathParam("roleId") String roleId, List<RoleUpdateCommand> commands);
  
  @GET @Path("roles/{roleId}") @Produces(MediaType.APPLICATION_JSON)
  Uni<Role> getRoleById(@PathParam("roleId") String roleId);
  
}
