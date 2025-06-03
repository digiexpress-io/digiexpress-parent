package io.digiexpress.eveli.client.web.resources.worker;

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

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.userprofile.client.api.UserProfileClient;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.UserProfileUpdateCommand;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/worker/rest/api/userprofiles")
@RequiredArgsConstructor
public class UserProfileController {

  private final UserProfileClient userProfileClient;
  private final WorkerAuthClient workerAuthClient;

  @GetMapping
  public Uni<List<UserProfile>> findAllUserProfiles() {
    return userProfileClient.userProfileQuery().findAll();
  }
  @GetMapping("/{id}")
  public Uni<UserProfile> getUserProfileById(@PathVariable("id") String profileId) {
    if("current".equals(profileId)) {
      return userProfileClient.userProfileQuery().get(workerAuthClient.getUser().getPrincipal().getUsername());
    }
    return userProfileClient.userProfileQuery().get(profileId);
  }
  @PostMapping
  public Uni<UserProfile> createUserProfile(CreateUserProfile command) {
    return userProfileClient.createUserProfile().createOne(command);
  }
  
  @PutMapping("/{id}")
  public Uni<UserProfile> updateUserProfile(@PathVariable("id") String profileId, List<UserProfileUpdateCommand> commands) {
    return userProfileClient.updateUserProfile().updateOne(commands);
  }
  @DeleteMapping("/{id}")
  public Uni<UserProfile> deleteUserProfile(@PathVariable("id") String profileId, UserProfileUpdateCommand command) {
    return userProfileClient.updateUserProfile().updateOne(command);
  }
}
