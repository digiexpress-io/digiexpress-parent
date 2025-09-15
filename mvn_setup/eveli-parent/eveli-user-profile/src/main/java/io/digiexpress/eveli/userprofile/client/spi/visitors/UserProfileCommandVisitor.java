package io.digiexpress.eveli.userprofile.client.spi.visitors;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.digiexpress.eveli.userprofile.client.api.model.ImmutableCreateUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableNotificationSetting;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableUserDetails;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.ArchiveUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.ChangeNotificationSetting;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.ChangeTenantFeatures;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.ChangeUserDetailsEmail;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.ChangeUserDetailsFirstName;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.ChangeUserDetailsLastName;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.UpsertUserProfile;
import io.resys.thena.spi.ThenaDocConfig;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserProfileCommandVisitor {
  @SuppressWarnings("unused")
  private final UserProfile start;
  private final List<UserProfileCommand> visitedCommands = new ArrayList<>();
  private ImmutableUserProfile current;
  
  public UserProfileCommandVisitor(ThenaDocConfig ctx) {
    this.start = null;
    this.current = null;
  }
  
  public UserProfileCommandVisitor(UserProfile start, ThenaDocConfig ctx) {
    this.start = start;
    this.current = ImmutableUserProfile.builder().from(start).build(); 
  }
  
  public Tuple2<UserProfile, List<JsonObject>> visitTransaction(List<? extends UserProfileCommand> commands) throws NoChangesException {
    for(final var command : commands) {
      visitCommand(command);
    }
    
    if(visitedCommands.isEmpty()) {
      throw new NoChangesException();
    }
    // don't bother logging ui settings to commands
    final var loggedCommands = visitedCommands.stream()
        .map(JsonObject::mapFrom)
        .collect(Collectors.toList());

    return Tuple2.of(this.current, loggedCommands);
  }
  
  private UserProfile visitCommand(UserProfileCommand command) throws NoChangesException {
    switch (command.getCommandType()) {
    case CreateUserProfile:
      return visitCreateUserProfile((CreateUserProfile) command);
    case UpsertUserProfile:
      return visitUpsertUserProfile((UpsertUserProfile) command);
    case ChangeUserDetailsFirstName:
      return visitChangeUserDetailsFirstName((ChangeUserDetailsFirstName) command);
    case ChangeUserDetailsLastName:
      return visitChangeUserDetailsLastName((ChangeUserDetailsLastName) command);
    case ChangeUserDetailsEmail:
      return visitChangeUserDetailsEmail((ChangeUserDetailsEmail) command);
    case ChangeNotificationSetting:
      return visitChangeNotificationSetting((ChangeNotificationSetting) command);
    case ArchiveUserProfile:
      return visitArchiveUserProfile((ArchiveUserProfile) command);
    case ChangeTenantFeatures:
      return visitChangeTenantFeatures((ChangeTenantFeatures) command);
    }
    
    throw new UpdateUserProfileVisitorException(String.format("Unsupported command type: %s, body: %s", command.getClass().getSimpleName(), command.toString())); 
  }
  
  private ImmutableUserDetails createDetails(CreateUserProfile command) {
    final var firstName = Optional.ofNullable(command.getFirstName()).orElse(createFirstName(command));
    final var lastName = Optional.ofNullable(command.getLastName()).orElse(createLastName(command));
    
    final var details = ImmutableUserDetails.builder()
        .username(Optional.ofNullable(command.getUsername()).orElse(createUserName(command)))
        .firstName(firstName)
        .lastName(lastName)
        .email(command.getEmail())
        .build();
    
    return details;
  }
  
  private String createUserName(CreateUserProfile command) {
    final var email = command.getEmail();
    final var splitAt = email.indexOf("@");
    if(splitAt <= 0) {
      return email;
    }
    return email.substring(splitAt);
  }
  private String createFirstName(CreateUserProfile command) {
    final var email = command.getEmail();
    final var frags = email.split("\\.");
    return StringUtils.capitalize(frags[0]);
  }
  private String createLastName(CreateUserProfile command) {
    final var userName = createUserName(command);
    final var frags = userName.split("\\.");
    if(frags.length == 1) {
      return StringUtils.capitalize(frags[0]);
    }
    return StringUtils.capitalize(frags[1]);
  }
  private UserProfile visitCreateUserProfile(CreateUserProfile command) {
    final var id = command.getId();
    this.current = ImmutableUserProfile.builder()
      .id(id)
      .details(createDetails(command))
      .tenantFeatures(command.getTenantFeatures())
      .notificationSettings(command.getNotificationSettings().stream()
          .map(e -> ImmutableNotificationSetting.builder()
              .from(e)
              .build())
          .toList())
      .build();
    log.debug("Creating user profile: {}", this.current);
    visitedCommands.add(command);
    
    return this.current;
  }


  private UserProfile visitUpsertUserProfile(UpsertUserProfile command) {
    if(this.current == null) {
      final var id = command.getId();
      final var details = createDetails(ImmutableCreateUserProfile.builder()
          .id(id)
          .username(command.getUsername())
          .firstName(command.getFirstName())
          .lastName(command.getLastName())
          .email(command.getEmail())
          .id(id)
      .build());
      this.current = ImmutableUserProfile.builder()
          .id(id)
          .details(details)
          .tenantFeatures(command.getTenantFeatures())
          .notificationSettings(command.getNotificationSettings().stream()
              .map(e -> ImmutableNotificationSetting.builder()
                  .from(e)
                  .build())
              .toList())
          .build();
      visitedCommands.add(command);
      return this.current;
    }
    
    return this.current;
  }
  
  private UserProfile visitChangeUserDetailsFirstName(ChangeUserDetailsFirstName command) {
    this.current = this.current
        .withId(current.getId())
        .withDetails(ImmutableUserDetails.builder()
            .from(this.current.getDetails())
            .firstName(command.getFirstName())
            .build());
    visitedCommands.add(command);
    return this.current;
  } 
  private UserProfile visitChangeUserDetailsLastName(ChangeUserDetailsLastName command) {
    this.current = this.current
        .withId(current.getId())
        .withDetails(ImmutableUserDetails.builder()
            .from(this.current.getDetails())
            .lastName(command.getLastName())
            .build());
    visitedCommands.add(command);
    return this.current;
  }
  private UserProfile visitChangeUserDetailsEmail(ChangeUserDetailsEmail command) {
    this.current = this.current
        .withId(current.getId())
        .withDetails(ImmutableUserDetails.builder()
            .from(this.current.getDetails())
            .email(command.getNewEmail())
            .build());
    visitedCommands.add(command);
    return this.current;
  }
  
  private UserProfile visitChangeTenantFeatures(ChangeTenantFeatures command) {
    this.current = this.current
        .withId(current.getId())
        .withTenantFeatures(command.getTenantFeatures());
    visitedCommands.add(command);
    return this.current;
  }
  private UserProfile visitChangeNotificationSetting(ChangeNotificationSetting command) {
    this.current = this.current
        .withId(current.getId())
        .withNotificationSettings(command.getNotificationSettings());
    visitedCommands.add(command);
    return this.current;
  }
  
  //TODO 
  private UserProfile visitArchiveUserProfile(ArchiveUserProfile command) {;
    visitedCommands.add(command);
    return this.current;
  }
  
  public static class NoChangesException extends Exception {
    private static final long serialVersionUID = 5955370217897065513L;
  }

  public static class UpdateUserProfileVisitorException extends RuntimeException {
    private static final long serialVersionUID = -1385190644836838881L;

    public UpdateUserProfileVisitorException(String message, Throwable cause) {
      super(message, cause);
    }

    public UpdateUserProfileVisitorException(String message) {
      super(message);
    }
  }
}
