package io.resys.permission.client.api.model;

import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.thena.docdb.api.models.ThenaEnvelope.ThenaObjects;

@Value.Immutable @JsonSerialize(as = ImmutableRoleHierarchyContainer.class) @JsonDeserialize(as = ImmutableRoleHierarchyContainer.class)
public interface RoleHierarchyContainer extends ThenaObjects {
  String getTargetRoleId();
  Map<String, Role> getRoles();
  Map<String, Permission> getPermissions(); // permissions by name
  Map<String, Principal> getPrincipals();   // principals by name
  String getLog();
}
