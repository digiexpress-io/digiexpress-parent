package io.resys.avatar.client.spi.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.DocumentType;

import io.resys.avatar.client.api.Avatar;
import io.resys.avatar.client.api.AvatarCommand;
import io.resys.avatar.client.api.AvatarCommand.ChangeAvatarColorCode;
import io.resys.avatar.client.api.AvatarCommand.ChangeAvatarDisplayName;
import io.resys.avatar.client.api.AvatarCommand.ChangeAvatarLetterCode;
import io.resys.avatar.client.api.AvatarCommand.CreateAvatar;
import io.resys.avatar.client.spi.store.AvatarStoreConfig;
import io.resys.avatar.client.spi.support.ColorProvider;
import io.resys.avatar.client.spi.support.LetterCodeProvider;
import io.resys.userprofile.client.api.ImmutableAvatar;
import io.resys.userprofile.client.api.ImmutableChangeAvatarLetterCode;


public class AvatarCommandVisitor {
  private final Avatar start;
  private final List<AvatarCommand> visitedCommands = new ArrayList<>();
  @SuppressWarnings("unused")
  private final List<Avatar> allExistingProfiles;
  private final List<String> letterCodes = new ArrayList<>();
  private final List<String> colorCodes = new ArrayList<>();
  private final List<String> displayNames = new ArrayList<>();
  private ImmutableAvatar current;
  
  public AvatarCommandVisitor(AvatarStoreConfig ctx, List<Avatar> allExistingProfiles) {
    this.start = null;
    this.current = null;
    this.allExistingProfiles = allExistingProfiles;
    
    for(final var profile : allExistingProfiles) {
      letterCodes.add(profile.getLetterCode());
      colorCodes.add(profile.getColorCode());
      displayNames.add(profile.getDisplayName());
    } 
  }
  
  public AvatarCommandVisitor(Avatar start, AvatarStoreConfig ctx, List<Avatar> allExistingProfiles) {
    this.start = start;
    this.current = ImmutableAvatar.builder().from(start).build();
    this.allExistingProfiles = allExistingProfiles;
    
    for(final var profile : allExistingProfiles) {
      if(profile.getId().equals(start.getId())) {
        continue;
      }
      letterCodes.add(profile.getLetterCode());
      colorCodes.add(profile.getColorCode());
      displayNames.add(profile.getDisplayName());
    } 
  }
  
  public Avatar visitTransaction(List<? extends AvatarCommand> commands) throws NoChangesException {
    for(final var command : commands) {
      visitCommand(command);
    }
    
    if(visitedCommands.isEmpty()) {
      throw new NoChangesException();
    }
    return this.current;
  }
  
  private Avatar visitCommand(AvatarCommand command) throws NoChangesException {
    switch (command.getCommandType()) {
    case CREATE_AVATAR:
      return visitCreateAvatar((CreateAvatar) command);
    case CHANGE_COLOR_CODE:
      return visitChangeAvatarColorCode((ChangeAvatarColorCode) command);
    case CHANGE_DISPLAY_NAME:
      return visitChangeAvatarDisplayName((ChangeAvatarDisplayName) command);
    case CHANGE_LETTER_CODE:
      return visitChangeAvatarLetterCode((ChangeAvatarLetterCode) command);
    }
    
    throw new UpdateAvatarVisitorException(String.format("Unsupported command type: %s, body: %s", command.getClass().getSimpleName(), command.toString())); 
  }
  private ImmutableUserDetails createDetails(CreateAvatar command) {
    final var firstName = Optional.ofNullable(command.getFirstName()).orElse(createFirstName(command));
    final var lastName = Optional.ofNullable(command.getLastName()).orElse(createLastName(command));
    
    final var details = ImmutableUserDetails.builder()
        .username(Optional.ofNullable(command.getUsername()).orElse(createUserName(command)))
        .firstName(firstName)
        .lastName(lastName)
        .colorCode(Optional.ofNullable(command.getColorCode()).orElse(createColorCode(command)))
        .letterCode(Optional.ofNullable(command.getLetterCode()).orElse(LetterCodeProvider.getInstance(colorCodes, firstName, lastName).getNextCode()))
        .displayName(firstName + " " + lastName)
        .email(command.getEmail())
        .build();
    
    this.colorCodes.add(details.getColorCode());
    this.letterCodes.add(details.getLetterCode());
    this.displayNames.add(details.getDisplayName());
    return details;
  }
  
  private String createUserName(CreateAvatar command) {
    final var email = command.getEmail();
    final var splitAt = email.indexOf("@");
    if(splitAt <= 0) {
      return email;
    }
    return email.substring(splitAt);
  }
  private String createFirstName(CreateAvatar command) {
    final var email = command.getEmail();
    final var frags = email.split("\\.");
    return StringUtils.capitalize(frags[0]);
  }
  private String createLastName(CreateAvatar command) {
    final var userName = createUserName(command);
    final var frags = userName.split("\\.");
    if(frags.length == 1) {
      return StringUtils.capitalize(frags[0]);
    }
    return StringUtils.capitalize(frags[1]);
  }
  private String createColorCode(CreateAvatar command) {
    return ColorProvider.getInstance(colorCodes).getNextColor();
  }
  private Avatar visitCreateAvatar(CreateAvatar command) {
    final var id = command.getId();
    final var targetDate = requireTargetDate(command);
    this.current = ImmutableAvatar.builder()
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
          ImmutableAvatarTransaction.builder()
          .id("1")
          .addCommands(command)
          .build())
      .documentType(DocumentType.USER_PROFILE)
      .build();
    visitedCommands.add(command);
    
    return this.current;
  }


  
  private Avatar visitChangeAvatarColorCode(ChangeAvatarColorCode command) {
    this.current = this.current.withColorCode(command.getColorCode());
    visitedCommands.add(command);
    return this.current;
  }
   
  private Avatar visitChangeAvatarDisplayName(ChangeAvatarDisplayName command) {
    this.current = this.current.withDisplayName(command.getDisplayName());
    visitedCommands.add(command);
    return this.current;
  }
  
  private Avatar visitChangeAvatarLetterCode(ChangeAvatarLetterCode command) {
    this.current = this.current.withLetterCode(command.getLetterCode());
    visitedCommands.add(command);
    return this.current;
  }
  public static class NoChangesException extends Exception {
    private static final long serialVersionUID = 5955370217897065513L;
  }

  public static class UpdateAvatarVisitorException extends RuntimeException {
    private static final long serialVersionUID = -1385190644836838881L;

    public UpdateAvatarVisitorException(String message, Throwable cause) {
      super(message, cause);
    }

    public UpdateAvatarVisitorException(String message) {
      super(message);
    }
  }
}
