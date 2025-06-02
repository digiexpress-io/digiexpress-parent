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

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

import io.digiexpress.eveli.userprofile.client.api.model.UserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.UserProfileUpdateCommand;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.PathParam;



public interface UserProfileRestApi {
  
  @GetMapping(name = "userprofiles", produces = MediaType.APPLICATION_JSON_VALUE)
  Uni<List<UserProfile>> findAllUserProfiles();
  
  @GetMapping(name = "userprofiles/{profileId}", produces = MediaType.APPLICATION_JSON_VALUE)
  Uni<UserProfile> getUserProfileById(@PathParam("profileId") String profileId);
  
  @GetMapping(name = "userprofiles", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  Uni<UserProfile> createUserProfile(CreateUserProfile command);

  @PutMapping(name = "userprofiles/{profileId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  Uni<UserProfile> updateUserProfile(@PathParam("profileId") String profileId, List<UserProfileUpdateCommand> commands);
  
  @DeleteMapping(name = "userprofiles/{profileId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  Uni<UserProfile> deleteUserProfile(@PathParam("profileId") String profileId, UserProfileUpdateCommand command);
  
}
