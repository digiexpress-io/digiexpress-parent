package io.resys.permission.client.api.model;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;



@Value.Immutable @JsonSerialize(as = ImmutablePrincipal.class) @JsonDeserialize(as = ImmutablePrincipal.class)
public interface Principal {
  String getId();
  String getVersion();
  String getName();
  String getEmail();
  
  List<Role> getRoles();
  ActorStatus getStatus();
  

  @Value.Immutable @JsonSerialize(as = ImmutableRole.class) @JsonDeserialize(as = ImmutableRole.class)
  interface Role {
    String getId();
    String getVersion();
    String getName();
    String getDescription();
    List<Permission> getPermissions();
    ActorStatus getStatus();
  }

  @Value.Immutable @JsonSerialize(as = ImmutablePermission.class) @JsonDeserialize(as = ImmutablePermission.class)
  interface Permission {
    String getId();
    String getVersion();
    String getName();  // example service.resource.verb --> pubsub.subscriptions.consume
    String getDescription();
    ActorStatus getStatus();
  }

  enum ActorStatus {
    ENABLED, DISABLED
  }
} 