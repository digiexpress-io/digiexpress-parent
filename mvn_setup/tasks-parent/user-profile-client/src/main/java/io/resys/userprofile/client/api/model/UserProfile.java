package io.resys.userprofile.client.api.model;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableUserProfile.class) @JsonDeserialize(as = ImmutableUserProfile.class)
public interface UserProfile extends Document {
  //String getExternalId(); //oauth2 sub
  Instant getCreated();
  Instant getUpdated();
  UserDetails getDetails();
  List<NotificationSetting> getNotificationSettings();
    
 @Value.Immutable @JsonSerialize(as = ImmutableUserDetails.class) @JsonDeserialize(as = ImmutableUserDetails.class)
  interface UserDetails {
    @Nullable String getUsername();
    @Nullable String getFirstName();
    @Nullable String getLastName();
    @Nullable String getEmail();
  }
 
 @Value.Immutable @JsonSerialize(as = ImmutableNotificationSetting.class) @JsonDeserialize(as = ImmutableNotificationSetting.class)
 interface NotificationSetting {
   String getType();
   Boolean getEnabled();
 }
   
  List<UserProfileTransaction> getTransactions(); 
  @Value.Default default DocumentType getDocumentType() { return DocumentType.USER_PROFILE; }
  
  @Value.Immutable @JsonSerialize(as = ImmutableUserProfileTransaction.class) @JsonDeserialize(as = ImmutableUserProfileTransaction.class)
  interface UserProfileTransaction extends Serializable {
    String getId();
    List<UserProfileCommand> getCommands(); 
  }
}
