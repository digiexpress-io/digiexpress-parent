package io.resys.permission.client.spi;

import java.util.Arrays;
import java.util.List;

import io.resys.permission.client.api.PermissionClient.UpdateRoleAction;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.permission.client.api.model.RoleCommand.ChangeRoleDescription;
import io.resys.permission.client.api.model.RoleCommand.RoleUpdateCommand;
import io.resys.thena.docdb.api.actions.OrgCommitActions.ModifyOneParty;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateRoleActionImpl implements UpdateRoleAction {
  private final PermissionStore ctx;

  @Override
  public Uni<Role> updateOne(RoleUpdateCommand command) {
    return updateOne(Arrays.asList(command));
  }

  @Override
  public Uni<Role> updateOne(List<RoleUpdateCommand> commands) {
    final ModifyOneParty modifyOneParty = ctx.getOrg().commit().modifyOneParty();
    for (RoleUpdateCommand command : commands) {
      switch(command.getCommandType()) {
      
      case CHANGE_ROLE_DESCRIPTION: {
        ChangeRoleDescription description = (ChangeRoleDescription) command;
        modifyOneParty.partyDescription(description.getDescription());
        break;
      }
      
      case CHANGE_ROLE_NAME: {
        
      }
      
      case CHANGE_ROLE_STATUS: {
        
      }
      
      case CHANGE_ROLE_PERMISSIONS: {
        
      }
      
      default: 
      }
    }
    
    
    return null;
  }

  @Override
  public Uni<List<Role>> updateMany(List<? extends RoleUpdateCommand> commands) {
    // TODO Auto-generated method stub
    return null;
  }

}
