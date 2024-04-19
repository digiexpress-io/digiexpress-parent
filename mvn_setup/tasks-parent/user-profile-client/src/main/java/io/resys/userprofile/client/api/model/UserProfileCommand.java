package io.resys.userprofile.client.api.model;

/*-
 * #%L
 * thena-tasks-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.userprofile.client.api.model.UserProfile.NotificationSetting;
import io.resys.userprofile.client.api.model.UserProfile.UiSettings;



@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "commandType")
@JsonSubTypes({
  @Type(value = ImmutableCreateUserProfile.class, name = "CreateUserProfile"),  
  @Type(value = ImmutableUpsertUserProfile.class, name = "UpsertUserProfile"),  
  @Type(value = ImmutableChangeUserDetailsFirstName.class, name = "ChangeUserDetailsFirstName"),  
  @Type(value = ImmutableChangeUserDetailsLastName.class, name = "ChangeUserDetailsLastName"),  
  @Type(value = ImmutableChangeUserDetailsEmail.class, name = "ChangeUserDetailsEmail"),  
  @Type(value = ImmutableChangeNotificationSetting.class, name = "ChangeNotificationSetting"),
  @Type(value = ImmutableArchiveUserProfile.class, name = "ArchiveUserProfile"),
  @Type(value = ImmutableUpsertUiSettings.class, name = "UpsertUiSettings")
})
public interface UserProfileCommand extends Serializable {
  String getId();
  @Nullable String getUserId();
  @Nullable Instant getTargetDate();
  UserProfileCommandType getCommandType();
  
  
  UserProfileCommand withUserId(String userId);
  UserProfileCommand withTargetDate(Instant targetDate);
  
  enum UserProfileCommandType {
    CreateUserProfile, 
    UpsertUserProfile, 
    ChangeUserDetailsFirstName,
    ChangeUserDetailsLastName,
    ChangeUserDetailsEmail,
    ChangeNotificationSetting,
    ArchiveUserProfile,
    UpsertUiSettings
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCreateUserProfile.class) @JsonDeserialize(as = ImmutableCreateUserProfile.class)
  interface CreateUserProfile extends UserProfileCommand {

    @Nullable String getUsername();
    @Nullable String getFirstName();
    @Nullable String getLastName();
    @Nullable String getColorCode();
    @Nullable String getLetterCode();
    @Nullable String getDisplayName();
    
    String getEmail();
    
    List<NotificationSetting> getNotificationSettings();
    
    @Value.Default
    @Override default UserProfileCommandType getCommandType() { return UserProfileCommandType.CreateUserProfile; }
  }
  
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "commandType")
  @JsonSubTypes({
    @Type(value = ImmutableUpsertUserProfile.class, name = "UpsertUserProfile"),  
    @Type(value = ImmutableChangeUserDetailsFirstName.class, name = "ChangeUserDetailsFirstName"),  
    @Type(value = ImmutableChangeUserDetailsLastName.class, name = "ChangeUserDetailsLastName"),  
    @Type(value = ImmutableChangeUserDetailsEmail.class, name = "ChangeUserDetailsEmail"),  
    @Type(value = ImmutableChangeNotificationSetting.class, name = "ChangeNotificationSetting"),
    @Type(value = ImmutableArchiveUserProfile.class, name = "ArchiveUserProfile"),
    @Type(value = ImmutableUpsertUiSettings.class, name = "UpsertUiSettings")
  })
  interface UserProfileUpdateCommand extends UserProfileCommand {
    UserProfileUpdateCommand withUserId(String userId);
    UserProfileUpdateCommand withTargetDate(Instant targetDate);
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableUpsertUserProfile.class) @JsonDeserialize(as = ImmutableUpsertUserProfile.class)
  interface UpsertUserProfile extends UserProfileUpdateCommand {
    @Nullable String getUsername();
    @Nullable String getFirstName();
    @Nullable String getLastName();
    String getEmail();
    List<NotificationSetting> getNotificationSettings();
    @Override default UserProfileCommandType getCommandType() { return UserProfileCommandType.UpsertUserProfile; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeUserDetailsFirstName.class) @JsonDeserialize(as = ImmutableChangeUserDetailsFirstName.class)
  interface ChangeUserDetailsFirstName extends UserProfileUpdateCommand {
    String getFirstName();
    @Override default UserProfileCommandType getCommandType() { return UserProfileCommandType.ChangeUserDetailsFirstName; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeUserDetailsLastName.class) @JsonDeserialize(as = ImmutableChangeUserDetailsLastName.class)
  interface ChangeUserDetailsLastName extends UserProfileUpdateCommand {
    String getLastName();
    @Override default UserProfileCommandType getCommandType() { return UserProfileCommandType.ChangeUserDetailsLastName; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeUserDetailsEmail.class) @JsonDeserialize(as = ImmutableChangeUserDetailsEmail.class)
  interface ChangeUserDetailsEmail extends UserProfileUpdateCommand {
    String getNewEmail();
    @Override default UserProfileCommandType getCommandType() { return UserProfileCommandType.ChangeUserDetailsEmail; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeNotificationSetting.class) @JsonDeserialize(as = ImmutableChangeNotificationSetting.class)
  interface ChangeNotificationSetting extends UserProfileUpdateCommand {
    String getType();
    Boolean getEnabled();
    @Override default UserProfileCommandType getCommandType() { return UserProfileCommandType.ChangeNotificationSetting; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableUpsertUiSettings.class) @JsonDeserialize(as = ImmutableUpsertUiSettings.class)
  interface UpsertUiSettings extends UserProfileUpdateCommand {
    UiSettings getUiSettings();
    @Override default UserProfileCommandType getCommandType() { return UserProfileCommandType.UpsertUiSettings; }
  }  

  @Value.Immutable @JsonSerialize(as = ImmutableArchiveUserProfile.class) @JsonDeserialize(as = ImmutableArchiveUserProfile.class)
  interface ArchiveUserProfile extends UserProfileUpdateCommand {
    @Override default UserProfileCommandType getCommandType() { return UserProfileCommandType.ArchiveUserProfile; }
  }

}
