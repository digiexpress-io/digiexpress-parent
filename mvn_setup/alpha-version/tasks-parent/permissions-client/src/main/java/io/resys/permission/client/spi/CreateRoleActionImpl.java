package io.resys.permission.client.spi;

import io.resys.permission.client.api.PermissionClient.CreateRoleAction;
import io.resys.permission.client.api.PermissionClient.RoleAccessEvaluator;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.permission.client.api.model.RoleCommand.CreateRole;
import io.resys.permission.client.api.model.RoleCommand.RoleCommandType;
import io.resys.thena.api.actions.OrgCommitActions.CreateOneParty;
import io.resys.thena.api.actions.OrgCommitActions.OnePartyEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateRoleActionImpl implements CreateRoleAction {
  private final PermissionStore ctx;

  @Override
  public Uni<Role> createOne(CreateRole command) {
    return createRequest(command)
        .onItem().transformToUni(response -> createResponse(response));
  }
  
  
  public Uni<OnePartyEnvelope> createRequest(CreateRole command) {
    final CreateOneParty createOneParty = ctx.getOrg(ctx.getConfig().getRepoId()).commit().createOneParty();

    if(command.getCommandType() == RoleCommandType.CREATE_ROLE) {
      final var role = (CreateRole) command;
    
    return createOneParty
        .message("created role")
        .author(ctx.getConfig().getAuthor().get())
        .partyDescription(role.getDescription())
        .partyName(role.getName())
        .parentId(role.getParentId())
        .addRightsToParty(role.getPermissions())
        .addMemberToParty(role.getPrincipals())
        .build();
    }
    
    throw new CreateRoleException("failed to create role"); 
  }
  
  public Uni<Role> createResponse(OnePartyEnvelope response) {
    if(response.getStatus() != CommitResultStatus.OK) {
      final var msg = "failed to created role";
      throw new CreateRoleException(msg, response);
    }
    
    final var role = response.getParty();
    return new RoleQueryImpl(ctx).get(role.getId());
  }
  
  
  
  final static class CreateRoleException extends RuntimeException {
    private static final long serialVersionUID = 542587084478526731L;
    
    public CreateRoleException(String message) {
      super(message);
    }

    public CreateRoleException(String message, OnePartyEnvelope response) {
      super(message + System.lineSeparator() + " " +
          String.join(System.lineSeparator() + " ", response.getMessages().stream().map(e -> e.getText()).toList()));
            response.getMessages().stream().filter(e -> e.getException() != null).forEach(e -> {
              if(e.getException() != null) {
                addSuppressed(e.getException());
              }
        });
    }

  }



  @Override
  public CreateRoleAction evalAccess(RoleAccessEvaluator eval) {
    // TODO Auto-generated method stub
    return null;
  }

}
