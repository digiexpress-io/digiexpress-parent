package io.resys.thena.tasks.dev.app.user;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.userprofile.client.api.model.UserProfile;
import jakarta.annotation.Nullable;



@Value.Immutable @JsonSerialize(as = ImmutableCurrentUserConfig.class) @JsonDeserialize(as = ImmutableCurrentUserConfig.class)
public interface CurrentUserConfig {

  @Nullable CurrentPermissions getPermissions();
  @Nullable CurrentTenant getTenant();
  @Nullable CurrentUser getUser();
  @Nullable UserProfile getProfile();
}
