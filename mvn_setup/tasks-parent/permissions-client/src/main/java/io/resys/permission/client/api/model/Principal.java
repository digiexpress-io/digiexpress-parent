package io.resys.permission.client.api.model;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.api.entities.org.OrgActorStatus;



@Value.Immutable @JsonSerialize(as = ImmutablePrincipal.class) @JsonDeserialize(as = ImmutablePrincipal.class)
public interface Principal {
  String getId();
  String getVersion();
  String getName();
  String getEmail();
  
  List<String> getRoles(); // all role names, irrelevant of inheritance 
  List<String> getPermissions(); // all permission names, irrelevant of inheritance 
  
  List<String> getDirectRoles(); // explicitly-given membership in the given role
  List<String> getDirectRolePermissions(); // inherited from the role that is directly connected to the principal
  List<String> getDirectPermissions(); // explicitly given to this principal only
  
  OrgActorStatus.OrgActorStatusType getStatus();
  

  @Value.Immutable @JsonSerialize(as = ImmutableRole.class) @JsonDeserialize(as = ImmutableRole.class)
  interface Role {
    String getId();
    @Nullable String getParentId();
    String getVersion();
    String getName();
    String getDescription();
    List<String> getPermissions();  // permission names
    List<String> getPrincipals();   // user names
    OrgActorStatus.OrgActorStatusType getStatus();
  }

  @Value.Immutable @JsonSerialize(as = ImmutablePermission.class) @JsonDeserialize(as = ImmutablePermission.class)
  interface Permission {
    String getId();
    String getVersion();
    String getName();  // example service.resource.verb --> pubsub.subscriptions.consume
    String getDescription();
    OrgActorStatus.OrgActorStatusType getStatus();
  }



} 