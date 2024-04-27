package io.resys.thena.tasks.dev.app.user;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.permission.client.api.model.Principal;

@Value.Immutable @JsonSerialize(as = ImmutableCurrentPermissions.class) @JsonDeserialize(as = ImmutableCurrentPermissions.class)
public interface CurrentPermissions {
  Principal getPrincipal();
  List<String> getPermissions();
  List<String> getRoles();
}
