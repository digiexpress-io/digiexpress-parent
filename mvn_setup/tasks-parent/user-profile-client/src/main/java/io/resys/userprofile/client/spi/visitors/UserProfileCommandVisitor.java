package io.resys.userprofile.client.spi.visitors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.userprofile.client.api.model.Document.DocumentType;
import io.resys.userprofile.client.api.model.ImmutableNotificationSetting;
import io.resys.userprofile.client.api.model.ImmutableUserDetails;
import io.resys.userprofile.client.api.model.ImmutableUserProfile;
import io.resys.userprofile.client.api.model.ImmutableUserProfileTransaction;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand;
import io.resys.userprofile.client.api.model.UserProfileCommand.ArchiveUserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.ChangeNotificationSetting;
import io.resys.userprofile.client.api.model.UserProfileCommand.ChangeUserDetailsEmail;
import io.resys.userprofile.client.api.model.UserProfileCommand.ChangeUserDetailsFirstName;
import io.resys.userprofile.client.api.model.UserProfileCommand.ChangeUserDetailsLastName;
import io.resys.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.UpsertUserProfile;
import io.resys.userprofile.client.spi.store.DocumentConfig;


public class UserProfileCommandVisitor {
  private final DocumentConfig ctx;
  private final UserProfile start;
  private final List<UserProfileCommand> visitedCommands = new ArrayList<>();
  private ImmutableUserProfile current;
  
  public UserProfileCommandVisitor(DocumentConfig ctx) {
    this.start = null;
    this.current = null;
    this.ctx = ctx;
  }
  
  public UserProfileCommandVisitor(UserProfile start, DocumentConfig ctx) {
    this.start = start;
    this.current = ImmutableUserProfile.builder().from(start).build();
    this.ctx = ctx;
  }
  
  public UserProfile visitTransaction(List<? extends UserProfileCommand> commands) throws NoChangesException {
    commands.forEach(this::visitCommand);
    
    if(visitedCommands.isEmpty()) {
      throw new NoChangesException();
    }
    
    final var transactions = new ArrayList<>(start == null ? Collections.emptyList() : start.getTransactions());
    final var id = String.valueOf(transactions.size() +1);
    transactions
      .add(ImmutableUserProfileTransaction.builder()
        .id(id)
        .commands(visitedCommands)
        .build());
    this.current = this.current.withVersion(id).withTransactions(transactions);
    return this.current;
  }
  
  private UserProfile visitCommand(UserProfileCommand command) {
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
  
  
  private UserProfile visitCreateUserProfile(CreateUserProfile command) {
    final var id = command.getId();
    final var targetDate = requireTargetDate(command);
    
    this.current = ImmutableUserProfile.builder()
      .id(id)
      .details(ImmutableUserDetails.builder().from(command.getDetails()).build())
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
      this.current = ImmutableUserProfile.builder()
          .id(id)
          .details(ImmutableUserDetails.builder().from(command.getDetails()).build())
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
