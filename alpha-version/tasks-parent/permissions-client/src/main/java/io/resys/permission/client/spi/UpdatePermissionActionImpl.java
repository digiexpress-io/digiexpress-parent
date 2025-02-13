package io.resys.permission.client.spi;

import java.util.Arrays;
import java.util.List;

import io.resys.permission.client.api.PermissionClient.PermissionAccessEvaluator;
import io.resys.permission.client.api.PermissionClient.UpdatePermissionAction;
import io.resys.permission.client.api.model.ChangeType;
import io.resys.permission.client.api.model.ImmutablePermission;
import io.resys.permission.client.api.model.PermissionCommand.ChangePermissionDescription;
import io.resys.permission.client.api.model.PermissionCommand.ChangePermissionName;
import io.resys.permission.client.api.model.PermissionCommand.ChangePermissionPrincipals;
import io.resys.permission.client.api.model.PermissionCommand.ChangePermissionRoles;
import io.resys.permission.client.api.model.PermissionCommand.ChangePermissionStatus;
import io.resys.permission.client.api.model.PermissionCommand.PermissionUpdateCommand;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.thena.api.actions.OrgCommitActions.ModType;
import io.resys.thena.api.actions.OrgCommitActions.ModifyOneRight;
import io.resys.thena.api.actions.OrgCommitActions.OneRightEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.org.OrgActorStatusType;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class UpdatePermissionActionImpl implements UpdatePermissionAction {
  private final PermissionStore ctx;

  @Override
  public Uni<Permission> updateOne(PermissionUpdateCommand command) {
    return updateOne(Arrays.asList(command));
  }

  @Override
  public Uni<Permission> updateOne(List<PermissionUpdateCommand> commands) {
    final var ids = commands.stream().map(e -> e.getId()).distinct().toList();
    RepoAssert.isTrue(ids.size() == 1, () -> "Update commands must have same id because they are for the same right!");
    final var id = ids.get(0);
    
    return createRequest(id,  commands).onItem().transform(response -> createResponse(id, response)); 
  }
  

  public Uni<OneRightEnvelope> createRequest(String id, List<PermissionUpdateCommand> commands){
    
    final ModifyOneRight modifyOneRight = ctx.getOrg(ctx.getConfig().getRepoId()).commit().modifyOneRight();
    for(PermissionUpdateCommand command : commands) {
      switch(command.getCommandType()) {
      
      case CHANGE_PERMISSION_DESCRIPTION: {
        final var description = (ChangePermissionDescription) command;
        modifyOneRight.rightDescription(description.getDescription());
        break;
      }
      
      case CHANGE_PERMISSION_NAME: {
        final var name = (ChangePermissionName) command;
        modifyOneRight.rightName(name.getName());
        break;
      }
      
      case CHANGE_PERMISSION_STATUS: {
        final var status = (ChangePermissionStatus) command;
        modifyOneRight.status(status.getStatus());
        break;
      }
      
      case CHANGE_PERMISSION_PRINCIPALS: {
        final var principals = (ChangePermissionPrincipals) command;
        
        if(principals.getChangeType() == ChangeType.ADD) {
          principals.getPrincipals().forEach(principal -> modifyOneRight.modifyMember(ModType.ADD, principal));
          
        } else if(principals.getChangeType() == ChangeType.REMOVE) {
          principals.getPrincipals().forEach(principal -> modifyOneRight.modifyMember(ModType.REMOVE, principal));

        } else if(principals.getChangeType() == ChangeType.SET_ALL) {
          modifyOneRight.setAllMembers(principals.getPrincipals());
          
        } else {
          throw new UpdatePermissionException("Command type not found exception: " + command.getCommandType() + "/" + principals.getChangeType());
        }
        break;
      }
      
      case CHANGE_PERMISSION_ROLES: {
        final var roles = (ChangePermissionRoles) command;
        
        if(roles.getChangeType() == ChangeType.ADD) {
          roles.getRoles().forEach(role -> modifyOneRight.modifyParty(ModType.ADD, role));
          
        } else if(roles.getChangeType() == ChangeType.REMOVE) {
          roles.getRoles().forEach(role -> modifyOneRight.modifyParty(ModType.REMOVE, role)); 

        } else if(roles.getChangeType() == ChangeType.SET_ALL) {
          modifyOneRight.setAllParties(roles.getRoles()); 
          
        } else {
          throw new UpdatePermissionException("Command type not found exception: " + command.getCommandType() + "/" + roles.getChangeType());
        }
        break;
      }
      
      
      default: throw new UpdatePermissionException("Command type not found exception :" + command.getCommandType());
      }
    }
      return modifyOneRight
        .rightId(id)
        .message("Permission update")
        .author(ctx.getConfig().getAuthor().get())
        .build();
    }
  
  public Permission createResponse(String id, OneRightEnvelope response) {
    if(response.getStatus() != CommitResultStatus.OK) {
      final var msg = "failed to update permission by id='%s'!".formatted(id);
      throw new UpdatePermissionException(msg, response);
    }
    
    final var right = response.getRight();
    return ImmutablePermission.builder()
      .id(right.getId())
      .version(right.getCommitId())

      .description(right.getRightDescription())
      .name(right.getRightName())
      .status(OrgActorStatusType.IN_FORCE)
      .roles(response.getDirectParties().stream().map(party -> party.getPartyName()).toList()) 
      .principals(response.getDirectMembers().stream().map(member -> member.getUserName()).toList())
      .build();
    }


  public static class UpdatePermissionException extends RuntimeException {
    private static final long serialVersionUID = -6566691011186609788L;
    
    public UpdatePermissionException(String message, OneRightEnvelope response) {
      super(message + System.lineSeparator() + " " +
        String.join(System.lineSeparator() + " ", response.getMessages().stream().map(e -> e.getText()).toList()));
          response.getMessages().stream().filter(e -> e.getException() != null).forEach(e -> {
            addSuppressed(e.getException());
       });
    }

    public UpdatePermissionException(String message) {
      super(message);
    } 
  }


  @Override
  public UpdatePermissionAction evalAccess(PermissionAccessEvaluator eval) {
    // TODO Auto-generated method stub
    return null;
  }
}
