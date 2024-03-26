package io.resys.permission.client.spi;

import io.resys.permission.client.api.PermissionClient.CreateRoleAction;
import io.resys.permission.client.api.model.ImmutableRole;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.permission.client.api.model.RoleCommand.CreateRole;
import io.resys.permission.client.api.model.RoleCommand.RoleCommandType;
import io.resys.thena.api.actions.OrgCommitActions.CreateOneParty;
import io.resys.thena.api.actions.OrgCommitActions.OnePartyEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.api.entities.org.OrgActorStatus.OrgActorStatusType;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateRoleActionImpl implements CreateRoleAction {
  private final PermissionStore ctx;

  @Override
  public Uni<Role> createOne(CreateRole command) {
    return createRequest(command).onItem().transform(response -> createResponse(response));
  }
  
  
  public Role createResponse(OnePartyEnvelope response) {
    
    if(response.getStatus() != CommitResultStatus.OK) {
      final var msg = "failed to created role";
      throw new CreateRoleException(msg, response);
    }
    
    final var role = response.getParty();
    return ImmutableRole.builder()
        .id("")
        .version(role.getCommitId())

        .name(role.getPartyName())
        .description(role.getPartyDescription())
        .status(OrgActorStatus.OrgActorStatusType.IN_FORCE)
        .build();
  }
  
  
  public Uni<OnePartyEnvelope> createRequest(CreateRole command) {
    final CreateOneParty createOneParty = ctx.getOrg(ctx.getConfig().getRepoId()).commit().createOneParty();

    if(command.getCommandType() == RoleCommandType.CREATE_ROLE) {
      CreateRole role = (CreateRole) command;
    
    
    return createOneParty
        .message("created role")
        .author(ctx.getConfig().getAuthor().get())

        .partyDescription(role.getDescription())
        .partyName(role.getName())
        .parentId(role.getId())
        .build();
    }
    
    throw new CreateRoleException("failed to create role");
    
  }
  
  final static class CreateRoleException extends RuntimeException {
    private static final long serialVersionUID = 542587084478526731L;
    
    public CreateRoleException(String message) {
      super(message);
    }

    public CreateRoleException(String message, OnePartyEnvelope response) {
      super(message + System.lineSeparator() + " " +
          String.join(System.lineSeparator() + " ", response.getMessages().stream().map(e -> e.getText()).toList()));
            response.getMessages().forEach(e -> {
              addSuppressed(e.getException());
        });
    }

    
  }

}
