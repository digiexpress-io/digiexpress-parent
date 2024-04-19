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
  @Nullable List<UiSettings> getUiSettings(); // might not be defined at all
  List<NotificationSetting> getNotificationSettings();
    
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
   
  List<UserProfileTransaction> getTransactions(); 
  @Value.Default default DocumentType getDocumentType() { return DocumentType.USER_PROFILE; }
  
  @Value.Immutable @JsonSerialize(as = ImmutableUserProfileTransaction.class) @JsonDeserialize(as = ImmutableUserProfileTransaction.class)
  interface UserProfileTransaction extends Serializable {
    String getId();
    List<UserProfileCommand> getCommands(); 
  }

  @Value.Immutable @JsonSerialize(as = ImmutableUiSettings.class) @JsonDeserialize(as = ImmutableUiSettings.class)
  interface UiSettings extends Serializable {
    String getSettingsId();
    List<UiSettingForConfig> getConfig();
    List<UiSettingForVisibility> getVisibility();
    List<UiSettingsForSorting> getSorting();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableUiSettingForVisibility.class) @JsonDeserialize(as = ImmutableUiSettingForVisibility.class)
  interface UiSettingForVisibility extends Serializable {
    String getDataId();
    Boolean getEnabled();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableUiSettingForConfig.class) @JsonDeserialize(as = ImmutableUiSettingForConfig.class)
  interface UiSettingForConfig extends Serializable {
    String getDataId();
    String getValue();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableUiSettingsForSorting.class) @JsonDeserialize(as = ImmutableUiSettingsForSorting.class)
  interface UiSettingsForSorting extends Serializable {
    String getDataId();
    String getDirection();
  }
}
