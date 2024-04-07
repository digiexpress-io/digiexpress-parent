package io.resys.permission.client.spi;

import io.resys.permission.client.api.PermissionClient.CreatePermissionAction;
import io.resys.permission.client.api.model.ImmutablePermission;
import io.resys.permission.client.api.model.PermissionCommand.CreatePermission;
import io.resys.permission.client.api.model.PermissionCommand.PermissionCommandType;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.thena.api.actions.OrgCommitActions.CreateOneRight;
import io.resys.thena.api.actions.OrgCommitActions.OneRightEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.org.OrgActorStatus;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreatePermissionActionImpl implements CreatePermissionAction {
  private final PermissionStore ctx;

  @Override
  public Uni<Permission> createOne(CreatePermission command) {
    return createRequest(command).onItem().transform(response -> createResponse(response));
  }


  public Uni<OneRightEnvelope> createRequest(CreatePermission command){
    final CreateOneRight createOneRight = ctx.getOrg(ctx.getConfig().getRepoId()).commit().createOneRight();
    
      if(command.getCommandType() == PermissionCommandType.CREATE_PERMISSION) {
        final var permission = (CreatePermission) command; 
      
        return createOneRight
          .rightName(permission.getName())
          .rightDescription(permission.getDescription())
          .message("created permission")
          .author(ctx.getConfig().getAuthor().get())
          .addRightToParties(permission.getRoles())
          .build();
      }
      
    throw new CreatePermissionException("failed to create permission");
  }

  public Permission createResponse(OneRightEnvelope response) {
    if(response.getStatus() != CommitResultStatus.OK) {
      final var msg = "failed to create permission";
      throw new CreatePermissionException(msg, response);
    }
    
    final var permission = response.getRight();
    return ImmutablePermission.builder()
        .id(permission.getId())
        .version(permission.getCommitId())
        .status(OrgActorStatus.OrgActorStatusType.IN_FORCE)
        .description(permission.getRightDescription())
        .name(permission.getRightName())
        .roles(response.getDirectParties().stream().map(party -> party.getPartyName()).toList())
        .build();
  }

  public static class CreatePermissionException extends RuntimeException {
    private static final long serialVersionUID = -1801167758630048042L;

    
    public CreatePermissionException(String message, OneRightEnvelope response) {
      super(message + System.lineSeparator() + " " +
          String.join(System.lineSeparator() + " ", response.getMessages().stream().map(e -> e.getText()).toList()));
            response.getMessages().stream().filter(e -> e.getException() != null).forEach(e -> {
              addSuppressed(e.getException());
        });
    }
    
    public CreatePermissionException(String message) {
      super(message);
    }

    
  }
}
