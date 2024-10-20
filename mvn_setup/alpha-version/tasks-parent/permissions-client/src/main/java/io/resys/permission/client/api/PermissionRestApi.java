package io.resys.permission.client.api;

import java.util.List;

import io.resys.permission.client.api.model.PermissionCommand.CreatePermission;
import io.resys.permission.client.api.model.PermissionCommand.PermissionUpdateCommand;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.permission.client.api.model.PrincipalCommand.CreatePrincipal;
import io.resys.permission.client.api.model.PrincipalCommand.PrincipalUpdateCommand;
import io.resys.permission.client.api.model.RoleCommand.CreateRole;
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


public interface PermissionRestApi {
  
  @GET @Path("principals") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<Principal>> findAllPrincipals();
  
  @GET @Path("principals/{principalId}") @Produces(MediaType.APPLICATION_JSON)
  Uni<Principal> getPrincipalById(@PathParam("principalId") String principalId);
  
  @PUT @Path("principals/{principalId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Principal> updatePrincipal(@PathParam("principalId") String principalId, List<PrincipalUpdateCommand> commands);
  
  @POST @Path("principals") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Principal> createPrincipal(CreatePrincipal command);
  
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
  Uni<Role> createRole(CreateRole command);
  
  @PUT @Path("roles/{roleId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Role> updateRole(@PathParam("roleId") String roleId, List<RoleUpdateCommand> commands);
  
  @GET @Path("roles/{roleId}") @Produces(MediaType.APPLICATION_JSON)
  Uni<Role> getRoleById(@PathParam("roleId") String roleId);
  
}
