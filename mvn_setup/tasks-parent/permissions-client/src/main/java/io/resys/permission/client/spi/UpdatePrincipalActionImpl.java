package io.resys.permission.client.spi;

import java.util.Arrays;
import java.util.List;

import io.resys.permission.client.api.PermissionClient.PrincipalAccessEvaluator;
import io.resys.permission.client.api.PermissionClient.UpdatePrincipalAction;
import io.resys.permission.client.api.model.ImmutablePrincipal;
import io.resys.permission.client.api.model.PermissionCommand.ChangeType;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.PrincipalCommand.ChangePrincipalEmail;
import io.resys.permission.client.api.model.PrincipalCommand.ChangePrincipalName;
import io.resys.permission.client.api.model.PrincipalCommand.ChangePrincipalPermissions;
import io.resys.permission.client.api.model.PrincipalCommand.ChangePrincipalRoles;
import io.resys.permission.client.api.model.PrincipalCommand.ChangePrincipalStatus;
import io.resys.permission.client.api.model.PrincipalCommand.PrincipalUpdateCommand;
import io.resys.thena.api.actions.OrgCommitActions.ModType;
import io.resys.thena.api.actions.OrgCommitActions.ModifyOneMember;
import io.resys.thena.api.actions.OrgCommitActions.OneMemberEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.org.OrgActorStatus.OrgActorStatusType;
import io.resys.thena.support.RepoAssert;
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
    final ModifyOneMember modifyOneMember = ctx.getOrg(ctx.getConfig().getRepoId()).commit().modifyOneMember();

    for (PrincipalUpdateCommand command : commands) {
      switch(command.getCommandType()) {
      
      case CHANGE_PRINCIPAL_ROLES: {
        final var roles = (ChangePrincipalRoles) command;
        
        if(roles.getChangeType() == ChangeType.ADD) {
          roles.getRoles().forEach(role -> modifyOneMember.modifyParties(ModType.ADD, role));
          
        } else if(roles.getChangeType() == ChangeType.DISABLE) {          
          roles.getRoles().forEach(role -> modifyOneMember.modifyParties(ModType.DISABLED, role));
          
        } else if(roles.getChangeType() == ChangeType.REMOVE) {
          roles.getRoles().forEach(role -> modifyOneMember.modifyParties(ModType.REMOVE, role)); 
        }
        break;
      }
     
      case CHANGE_PRINCIPAL_PERISSIONS: {
        final var permissions = (ChangePrincipalPermissions) command;
        
        if(permissions.getChangeType() == ChangeType.ADD) {
          permissions.getPermissions().forEach(perm -> modifyOneMember.modifyRights(ModType.ADD, perm));
          
        } else if(permissions.getChangeType() == ChangeType.DISABLE) {
          permissions.getPermissions().forEach(perm -> modifyOneMember.modifyRights(ModType.DISABLED, perm));
          
        } else if(permissions.getChangeType() == ChangeType.REMOVE) {
          permissions.getPermissions().forEach(perm -> modifyOneMember.modifyRights(ModType.REMOVE, perm));
        }
        break;
      }
      
      case CHANGE_PRINCIPAL_STATUS: {
        final var status = (ChangePrincipalStatus) command;
        modifyOneMember.status(status.getStatus());
        break;
      } 
      
      case CHANGE_PRINCIPAL_NAME: {
        final var name = (ChangePrincipalName) command;
        modifyOneMember.userName(name.getName());
        break;
      }
      
      case CHANGE_PRINCIPAL_EMAIL: {
        final var email = (ChangePrincipalEmail) command;
        modifyOneMember.email(email.getEmail());
        break;
      }
      
      default: throw new UpdatePrincipalException("Command type not found exception: " + command.getCommandType());
      }
    }
    
    
    return modifyOneMember
      .memberId(id)
      .author(ctx.getConfig().getAuthor().get())
      .message("Principal update")
      .build();
  }
    
  
  public Principal createResponse(String id, OneMemberEnvelope response) {
    if(response.getStatus() != CommitResultStatus.OK) {
      final var msg = "failed to update principal by id='%s'!".formatted(id);
      throw new UpdatePrincipalException(msg, response);
    }

    
    final var principal = response.getMember();
    
    return ImmutablePrincipal.builder()
      .id(principal.getId())
      .version(principal.getCommitId())
      
      .name(principal.getUserName())
      .email(principal.getEmail())
     // .roles(null) //TODO
     // .permissions(null) //TODO
    
      .status(OrgActorStatusType.IN_FORCE)
      .build();
  }

  public static class UpdatePrincipalException extends RuntimeException {
    private static final long serialVersionUID = -3041737023950149699L;

    public UpdatePrincipalException(String message, OneMemberEnvelope response) {
      super(message + System.lineSeparator() + " " +
        String.join(System.lineSeparator() + " ", response.getMessages().stream().map(e -> e.getText()).toList()));
          response.getMessages().stream().filter(e -> e.getException() != null).forEach(e -> {
          addSuppressed(e.getException());
     });
    }
    
    public UpdatePrincipalException(String message) {
      super(message);
    }
  }

  @Override
  public UpdatePrincipalAction evalAccess(PrincipalAccessEvaluator eval) {
    // TODO Auto-generated method stub
    return null;
  }
}
