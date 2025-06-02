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

import java.util.List;

import io.digiexpress.eveli.userprofile.client.api.model.UserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.UserProfileUpdateCommand;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserProfileResource implements UserProfileRestApi {

  private final UserProfileClient userProfileClient;
  private final IdentitySupplier identitySupplier;

  @Override
  public Uni<List<UserProfile>> findAllUserProfiles() {
    return userProfileClient.userProfileQuery().findAll();
  }
  
  @Override
  public Uni<UserProfile> getUserProfileById(String profileId) {
    if("current".equals(profileId)) {
      return userProfileClient.userProfileQuery().get(identitySupplier.getPrincipalId());
    }
    return userProfileClient.userProfileQuery().get(profileId);
  }
  
  @Override
  public Uni<UserProfile> createUserProfile(CreateUserProfile command) {
    return userProfileClient.createUserProfile().createOne(command);
  }

  @Override
  public Uni<UserProfile> updateUserProfile(String profileId, List<UserProfileUpdateCommand> commands) {
    return userProfileClient.updateUserProfile().updateOne(commands);
  }

  @Override
  public Uni<UserProfile> deleteUserProfile(String profileId, UserProfileUpdateCommand command) {
    return userProfileClient.updateUserProfile().updateOne(command);
  }
  
  interface IdentitySupplier {
    String getPrincipalId();
  }
}
