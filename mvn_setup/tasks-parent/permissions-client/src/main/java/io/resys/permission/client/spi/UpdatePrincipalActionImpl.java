package io.resys.permission.client.spi;

import java.util.Arrays;
import java.util.List;

import io.resys.permission.client.api.PermissionClient.UpdatePrincipalAction;
import io.resys.permission.client.api.model.ImmutablePrincipal;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.PrincipalCommand.ChangePrincipalRoles;
import io.resys.permission.client.api.model.PrincipalCommand.ChangePrincipalStatus;
import io.resys.permission.client.api.model.PrincipalCommand.ChangeType;
import io.resys.permission.client.api.model.PrincipalCommand.PrincipalUpdateCommand;
import io.resys.thena.docdb.api.actions.OrgCommitActions.ModType;
import io.resys.thena.docdb.api.actions.OrgCommitActions.ModifyOneMember;
import io.resys.thena.docdb.api.actions.OrgCommitActions.OneMemberEnvelope;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdatePrincipalActionImpl implements UpdatePrincipalAction {
  
  private final PermissionStore ctx;

  @Override
  public Uni<Principal> updateOne(PrincipalUpdateCommand command) {
    return updateOne(Arrays.asList(command));
  }

  @Override
  public Uni<Principal> updateOne(List<PrincipalUpdateCommand> commands) {
    final var ids = commands.stream().map(c -> c.getId()).distinct().toList();
    RepoAssert.isTrue(ids.size() == 1, () -> "Update commands must have same id because they are for the same role!");
    final var id = ids.get(0);
         
    return createRequest(id, commands).onItem().transform(response -> createResponse(id, response));
  }
        
  public Uni<OneMemberEnvelope> createRequest(String id, List<PrincipalUpdateCommand> commands){
    final ModifyOneMember modifyOneMember = ctx.getOrg().commit().modifyOneMember();

    for (PrincipalUpdateCommand command : commands) {
      switch(command.getCommandType()) {
      
      case CHANGE_PRINCIPAL_ROLES: {
        ChangePrincipalRoles roles = (ChangePrincipalRoles) command;
        
        if(roles.getChangeType() == ChangeType.ADD) {
          modifyOneMember.roles(ModType.ADD, roles.getRoles());
          
        } else if(roles.getChangeType() == ChangeType.DISABLE) {
          modifyOneMember.roles(ModType.DISABLED, roles.getRoles());
          
        } else if(roles.getChangeType() == ChangeType.REMOVE) {
          //TODO:: not implemented
          throw new UpdatePrincipalException("Command type not implemented: " + command.getCommandType()); 
        }
        break;
      }
      
      case CHANGE_PRINCIPAL_STATUS: {
        ChangePrincipalStatus status = (ChangePrincipalStatus) command;
        /* TODO:: not implemented
         * break;
         */ 
      }    
      default: throw new UpdatePrincipalException("Command type not found exception: " + command.getCommandType());
      }
    }
    
    return modifyOneMember
      .userId(id)
      .repoId(ctx.getConfig().getRepoId())
      .author(ctx.getConfig().getAuthor().get())
      .message("Principal update")
      .build();
  }
    
  
  public Principal createResponse(String id, OneMemberEnvelope response) {
    if(response.getStatus() != Repo.CommitResultStatus.OK) {
      final var msg = "failed to update principal by id='%s'!".formatted(id);
      throw new UpdatePrincipalException(msg, response);
    }

    
    final var principal = response.getUser();
    
    return ImmutablePrincipal.builder()
      .id(principal.getId())
      .version(principal.getCommitId())
      
      .name(principal.getUserName())
      .email(principal.getEmail())
      .roles(null)
      .status(null)
      .build();
  }

  public static class UpdatePrincipalException extends RuntimeException {
    private static final long serialVersionUID = -3041737023950149699L;

    public UpdatePrincipalException(String message, OneMemberEnvelope response) {
      super(message + System.lineSeparator() + " " +
        String.join(System.lineSeparator() + " ", response.getMessages().stream().map(e -> e.getText()).toList()));
          response.getMessages().forEach(e -> {
          addSuppressed(e.getException());
     });
    }
    
    public UpdatePrincipalException(String message) {
      super(message);
    }
  }
}
