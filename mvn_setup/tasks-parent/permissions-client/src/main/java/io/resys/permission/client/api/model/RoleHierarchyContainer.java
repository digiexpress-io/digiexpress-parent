package io.resys.permission.client.api.model;

import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.api.model.Principal.Role;

@Value.Immutable @JsonSerialize(as = ImmutableRoleHierarchyContainer.class) @JsonDeserialize(as = ImmutableRoleHierarchyContainer.class)
public interface RoleHierarchyContainer {
  
  List<RoleHierarchy> getRoles();
  
  Map<String, Permission> getPermissions(); // permissions by name
  Map<String, Principal> getPrincipals();   // principals by name


  @Value.Immutable @JsonSerialize(as = ImmutableRoleHierarchy.class) @JsonDeserialize(as = ImmutableRoleHierarchy.class)  
  interface RoleHierarchy {
    Role getRole();
    List<RoleHierarchy> getChildren();      // all direct roles that have parentId as this role.id
    List<String> getPrincipals();           // principal names
    List<String> getDirectPermissions();    // permission names
    List<String> getInheritedPermissions(); // permission names
  }
}
