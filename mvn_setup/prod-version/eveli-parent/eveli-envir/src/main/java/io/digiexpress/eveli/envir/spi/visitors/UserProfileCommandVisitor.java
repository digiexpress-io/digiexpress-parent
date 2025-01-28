package io.resys.userprofile.client.spi.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.resys.thena.spi.ThenaDocConfig;
import io.resys.userprofile.client.api.model.ImmutableCreateUserProfile;
import io.resys.userprofile.client.api.model.ImmutableNotificationSetting;
import io.resys.userprofile.client.api.model.ImmutableUserDetails;
import io.resys.userprofile.client.api.model.ImmutableUserProfile;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand;
import io.resys.userprofile.client.api.model.UserProfileCommand.ArchiveUserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.ChangeNotificationSetting;
import io.resys.userprofile.client.api.model.UserProfileCommand.ChangeUserDetailsEmail;
import io.resys.userprofile.client.api.model.UserProfileCommand.ChangeUserDetailsFirstName;
import io.resys.userprofile.client.api.model.UserProfileCommand.ChangeUserDetailsLastName;
import io.resys.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.UpsertUserProfile;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;


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
      .notificationSettings(command.getNotificationSettings().stream()
          .map(e -> ImmutableNotificationSetting.builder()
              .from(e)
              .build())
          .toList())
      .build();
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
  
  //TODO 
  private UserProfile visitChangeUserDetailsLastName(ChangeUserDetailsLastName command) {
    visitedCommands.add(command);
    return this.current;
  }
  
  //TODO
  private UserProfile visitChangeUserDetailsEmail(ChangeUserDetailsEmail command) {
    visitedCommands.add(command);
    return this.current;
  }
  
  //TODO
  private UserProfile visitChangeNotificationSetting(ChangeNotificationSetting command) {
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
