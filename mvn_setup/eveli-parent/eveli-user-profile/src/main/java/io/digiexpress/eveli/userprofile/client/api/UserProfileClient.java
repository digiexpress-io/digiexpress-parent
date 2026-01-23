package io.digiexpress.eveli.userprofile.client.api;

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

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.digiexpress.eveli.userprofile.client.api.model.UiSettings;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UiSettingsCommand.UiSettingsUpdateCommand;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.UpsertUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.UserProfileUpdateCommand;
import io.resys.thena.api.entities.Tenant;
import io.smallrye.mutiny.Uni;

public interface UserProfileClient {

  RepositoryQuery repoQuery();
  Uni<Tenant> getRepo();
  UserProfileClient withRepoId(String repoId);
  
  CreateUserProfileAction createUserProfile();
  UpdateUserProfileAction updateUserProfile();
  UpdateUiSettingsAction updateUiSettings();
  UserProfileQuery userProfileQuery();
  
  UiSettingsQuery uiSettingsQuery();

  interface CreateUserProfileAction {
    Uni<UserProfile> createOne(CreateUserProfile command);
    Uni<UserProfile> createOne(UpsertUserProfile command);
    Uni<List<UserProfile>> createMany(List<? extends CreateUserProfile> commands);
    Uni<List<UserProfile>> upsertMany(List<? extends UpsertUserProfile> commands);
  }
  
  interface UpdateUiSettingsAction {
    Uni<UiSettings> updateOne(UiSettingsUpdateCommand command);
  }

  interface UpdateUserProfileAction {
    Uni<UserProfile> updateOne(UserProfileUpdateCommand command);
    Uni<UserProfile> updateOne(List<UserProfileUpdateCommand> commands);
    Uni<List<UserProfile>> updateMany(List<UserProfileUpdateCommand> commands);
  }

  interface UserProfileQuery {
    Uni<List<UserProfile>> findAll();
    Uni<List<UserProfile>> findByIds(Collection<String> profileIds);
    Uni<UserProfile> get(String profileId);
    Uni<List<UserProfile>> deleteAll(String committerId, Instant targetDate);
  }

  interface UiSettingsQuery {
    Uni<List<UiSettings>> findAll(String profileId);
    Uni<Optional<UiSettings>> findOne(String profileId, String settingsId);
    Uni<UiSettings> get(String profileId, String settingsId);
    Uni<Optional<UiSettings>> deleteOne(String profileId, String settingsId);
  }
  
  public interface RepositoryQuery {
    RepositoryQuery repoName(String repoName);
    UserProfileClient build();

    Uni<UserProfileClient> deleteAll();
    Uni<UserProfileClient> delete();
    Uni<UserProfileClient> create();
    Uni<UserProfileClient> createIfNot();
    Uni<Optional<UserProfileClient>> get();
  }

  class UserProfileNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 5706579544456750293L;

    public UserProfileNotFoundException(String message) {
      super(message);
    }
  }
  class UiSettingsNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 5706579544456750293L;

    public UiSettingsNotFoundException(String message) {
      super(message);
    }
  }
}
