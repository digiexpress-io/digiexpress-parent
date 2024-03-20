package io.resys.permission.client.spi;

import java.util.Arrays;
import java.util.List;

import io.resys.permission.client.api.PermissionClient.UpdatePermissionAction;
import io.resys.permission.client.api.model.PermissionCommand.ChangePermissionDescription;
import io.resys.permission.client.api.model.PermissionCommand.ChangePermissionName;
import io.resys.permission.client.api.model.PermissionCommand.ChangePermissionStatus;
import io.resys.permission.client.api.model.PermissionCommand.PermissionUpdateCommand;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.thena.docdb.api.actions.OrgCommitActions.ModifyOneRight;
import io.resys.thena.docdb.support.RepoAssert;
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
    final var id = commands.stream().map(e -> e.getId()).distinct().toList();
    RepoAssert.isTrue(id.size() == 1, () -> "Update commands must have same id because they are for the same right!");
    
    
    final ModifyOneRight modifyOneRight = ctx.getOrg().commit().modifyOneRight();
    for(PermissionUpdateCommand command : commands) {
      switch(command.getCommandType()) {
      
      case CHANGE_PERMISSION_DESCRIPTION: {
        ChangePermissionDescription description = (ChangePermissionDescription) command;
        modifyOneRight.rightDescription(description.getDescription());
        break;
      }
      
      case CHANGE_PERMISSION_NAME: {
        ChangePermissionName name = (ChangePermissionName) command;
        modifyOneRight.rightName(name.getName());
        break;
      }
      
      case CHANGE_PERMISSION_STATUS: {
        ChangePermissionStatus status = (ChangePermissionStatus) command;
        //TODO not implemented
        //break;
      }
      
      default: throw new UpdatePermissionException("Command type not found exception :" + command.getCommandType());
      }
    }
    
    return modifyOneRight
        .rightId(id.iterator().next())
        .repoId(ctx.getConfig().getRepoId())
        .message("Permission update")
        .author(ctx.getConfig().getAuthor().get())
        .build().onItem().transform(e -> null);
  }

  @Override
  public Uni<List<Permission>> updateMany(List<? extends PermissionUpdateCommand> commands) {
    // TODO Auto-generated method stub
    return null;
  }

  public static class UpdatePermissionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UpdatePermissionException(String message) {
      super(message);
    } 
  }
}
