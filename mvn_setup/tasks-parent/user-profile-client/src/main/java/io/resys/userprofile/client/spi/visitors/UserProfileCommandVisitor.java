package io.resys.userprofile.client.spi.visitors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.resys.userprofile.client.api.model.Document.DocumentType;
import io.resys.userprofile.client.api.model.ImmutableCreateUserProfile;
import io.resys.userprofile.client.api.model.ImmutableNotificationSetting;
import io.resys.userprofile.client.api.model.ImmutableUiSettingForConfig;
import io.resys.userprofile.client.api.model.ImmutableUiSettingForVisibility;
import io.resys.userprofile.client.api.model.ImmutableUiSettings;
import io.resys.userprofile.client.api.model.ImmutableUiSettingsForSorting;
import io.resys.userprofile.client.api.model.ImmutableUserDetails;
import io.resys.userprofile.client.api.model.ImmutableUserProfile;
import io.resys.userprofile.client.api.model.ImmutableUserProfileTransaction;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.api.model.UserProfile.UiSettings;
import io.resys.userprofile.client.api.model.UserProfileCommand;
import io.resys.userprofile.client.api.model.UserProfileCommand.ArchiveUserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.ChangeNotificationSetting;
import io.resys.userprofile.client.api.model.UserProfileCommand.ChangeUserDetailsEmail;
import io.resys.userprofile.client.api.model.UserProfileCommand.ChangeUserDetailsFirstName;
import io.resys.userprofile.client.api.model.UserProfileCommand.ChangeUserDetailsLastName;
import io.resys.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.UpsertUiSettings;
import io.resys.userprofile.client.api.model.UserProfileCommand.UpsertUserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.UserProfileCommandType;
import io.resys.userprofile.client.spi.store.UserProfileStoreConfig;


public class UserProfileCommandVisitor {
  private final UserProfile start;
  private final List<UserProfileCommand> visitedCommands = new ArrayList<>();
  private ImmutableUserProfile current;
  
  public UserProfileCommandVisitor(UserProfileStoreConfig ctx) {
    this.start = null;
    this.current = null;
  }
  
  public UserProfileCommandVisitor(UserProfile start, UserProfileStoreConfig ctx) {
    this.start = start;
    this.current = ImmutableUserProfile.builder().from(start).build(); 
  }
  
  public UserProfile visitTransaction(List<? extends UserProfileCommand> commands) throws NoChangesException {
    for(final var command : commands) {
      visitCommand(command);
    }
    
    if(visitedCommands.isEmpty()) {
      throw new NoChangesException();
    }
    
    // don't bother logging ui settings to commands
    final var loggedCommands = visitedCommands.stream().filter(d -> d.getCommandType() != UserProfileCommandType.UpsertUiSettings).collect(Collectors.toList());
    final var transactions = new ArrayList<>(start == null ? Collections.emptyList() : start.getTransactions());
    final var id = String.valueOf(transactions.size() +1);
    
    if(loggedCommands.isEmpty()) {
      this.current = this.current.withVersion(id);
      return this.current; 
    }
    transactions
      .add(ImmutableUserProfileTransaction.builder()
        .id(id)
        .commands(loggedCommands)
        .build());
    this.current = this.current.withVersion(id).withTransactions(transactions);
    return this.current;
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
    case UpsertUiSettings:
      return visitUpsertUiSettings((UpsertUiSettings) command);
    }
    
    throw new UpdateUserProfileVisitorException(String.format("Unsupported command type: %s, body: %s", command.getClass().getSimpleName(), command.toString())); 
  }
  private UserProfile visitUpsertUiSettings(UpsertUiSettings command) throws NoChangesException {
    final List<UiSettings> next = new ArrayList<>();
    final List<UiSettings> old = this.current.getUiSettings() != null ? this.current.getUiSettings() : Collections.emptyList();
    
    // deep copy, just in case of accidental json fields
    final var newEntity = ImmutableUiSettings.builder()
        .from(command.getUiSettings())
        .config(command.getUiSettings().getConfig().stream().map(e -> ImmutableUiSettingForConfig.builder().from(e).build()).toList())
        .sorting(command.getUiSettings().getSorting().stream().map(e -> ImmutableUiSettingsForSorting.builder().from(e).build()).toList())
        .visibility(command.getUiSettings().getVisibility().stream().map(e -> ImmutableUiSettingForVisibility.builder().from(e).build()).toList())
        .build();
    
    boolean claimed = false;
    for(final UiSettings entry : old) {
      if(entry.getSettingsId().equals(command.getUiSettings().getSettingsId())) {
        final var updated = newEntity;
        if(updated.equals(entry)) {
          throw new NoChangesException();
        }
        
        next.add(updated);
        claimed = true;
      } else {
        next.add(entry);
      }
    }
    
    if(!claimed) {
      next.add(newEntity);
    }
    
    this.current = this.current.withUiSettings(next);
    visitedCommands.add(command);
    return this.current;
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
    final var targetDate = requireTargetDate(command);
    this.current = ImmutableUserProfile.builder()
      .id(id)
      .details(createDetails(command))
      .notificationSettings(command.getNotificationSettings().stream()
          .map(e -> ImmutableNotificationSetting.builder()
              .from(e)
              .build())
          .toList())
      .created(targetDate)
      .updated(targetDate)
      
      .addTransactions(
          ImmutableUserProfileTransaction.builder()
          .id("1")
          .addCommands(command)
          .build())
      .documentType(DocumentType.USER_PROFILE)
      .build();
    visitedCommands.add(command);
    
    return this.current;
  }


  private UserProfile visitUpsertUserProfile(UpsertUserProfile command) {
    final var targetDate = requireTargetDate(command);
    if(this.current == null) {
      final var id = command.getId();
      final var details = createDetails(ImmutableCreateUserProfile.builder()
          .username(command.getUsername())
          .firstName(command.getFirstName())
          .lastName(command.getLastName())
          .email(command.getEmail())
      .build());
      this.current = ImmutableUserProfile.builder()
          .id(id)
          .details(details)
          .notificationSettings(command.getNotificationSettings().stream()
              .map(e -> ImmutableNotificationSetting.builder()
                  .from(e)
                  .build())
              .toList())
          .created(targetDate)
          .updated(targetDate)

          .addTransactions(
              ImmutableUserProfileTransaction.builder()
              .id("1")
              .addCommands(command)
              .build())
          .documentType(DocumentType.USER_PROFILE)
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
            .build())        
        .withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }
  
  //TODO 
  private UserProfile visitChangeUserDetailsLastName(ChangeUserDetailsLastName command) {
    this.current = this.current
        .withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }
  
  //TODO
  private UserProfile visitChangeUserDetailsEmail(ChangeUserDetailsEmail command) {
    this.current = this.current
        .withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }
  
  //TODO
  private UserProfile visitChangeNotificationSetting(ChangeNotificationSetting command) {
    this.current = this.current
        .withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }
  
  //TODO 
  private UserProfile visitArchiveUserProfile(ArchiveUserProfile command) {
    this.current = this.current
        .withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }
  
  
  public static Instant requireTargetDate(UserProfileCommand command) {
    final var targetDate = command.getTargetDate();
    if (targetDate == null) {
      throw new UpdateUserProfileVisitorException("targetDate not defined");
    }
    final var userId = command.getUserId();
    if (userId == null) {
      throw new UpdateUserProfileVisitorException("userId not defined");
    }
    return targetDate;
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
