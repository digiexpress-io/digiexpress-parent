package io.resys.userprofile.client.api.model;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableUserProfile.class) @JsonDeserialize(as = ImmutableUserProfile.class)
public interface UserProfile extends Serializable {
  String getId();
  UserDetails getDetails();
  List<NotificationSetting> getNotificationSettings();
  
  @Nullable String getVersion();
  @Nullable Instant getCreated();
  @Nullable Instant getUpdated();
  
    
  @Value.Immutable @JsonSerialize(as = ImmutableUserDetails.class) @JsonDeserialize(as = ImmutableUserDetails.class)
  interface UserDetails {
    @Nullable String getUsername();
    @Nullable String getFirstName();
    @Nullable String getLastName();
    String getEmail();
  }
 
 @Value.Immutable @JsonSerialize(as = ImmutableNotificationSetting.class) @JsonDeserialize(as = ImmutableNotificationSetting.class)
  interface NotificationSetting {
    String getType();
    Boolean getEnabled();
  }
}
