package io.digiexpress.eveli.userprofile.client.api.model;

/*-
 * #%L
 * eveli-user-profile
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import jakarta.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableUserProfile.class) @JsonDeserialize(as = ImmutableUserProfile.class)
public interface UserProfile extends Serializable {
  String getId();
  UserDetails getDetails();
  List<NotificationSetting> getNotificationSettings();
  List<String> getTenantFeatures();
  
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
