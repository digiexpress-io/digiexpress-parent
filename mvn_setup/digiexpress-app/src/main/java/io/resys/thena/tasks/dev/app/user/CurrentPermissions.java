package io.resys.thena.tasks.dev.app.user;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableCurrentPermissions.class) @JsonDeserialize(as = ImmutableCurrentPermissions.class)
public interface CurrentPermissions {

  List<String> getPermissions();
}
