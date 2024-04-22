package io.resys.permission.client.api.model;

import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.thena.api.envelope.ThenaContainer;

@Value.Immutable @JsonSerialize(as = ImmutableRoleHierarchyContainer.class) @JsonDeserialize(as = ImmutableRoleHierarchyContainer.class)
public interface RoleHierarchyContainer extends ThenaContainer {
  String getBottomRoleId(); // where it all ends, the object we are trying to find
  String getTopRoleId(); // where it all starts, tip of the tree
  
  Map<String, Role> getRoles();
  Map<String, Permission> getPermissions(); // permissions by id
  Map<String, Principal> getPrincipals();   // principals by id
  String getLog();
}
