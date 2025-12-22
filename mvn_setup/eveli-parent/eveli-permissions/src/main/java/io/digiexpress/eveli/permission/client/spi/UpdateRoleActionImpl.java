package io.digiexpress.eveli.permission.client.spi;

/*-
 * #%L
 * eveli-permissions
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Arrays;
import java.util.List;

import io.digiexpress.eveli.permission.client.api.PermissionClient.RoleAccessEvaluator;
import io.digiexpress.eveli.permission.client.api.PermissionClient.UpdateRoleAction;
import io.digiexpress.eveli.permission.client.api.model.ChangeType;
import io.digiexpress.eveli.permission.client.api.model.ImmutableRole;
import io.digiexpress.eveli.permission.client.api.model.Principal.Role;
import io.digiexpress.eveli.permission.client.api.model.RoleCommand.ChangeRoleDescription;
import io.digiexpress.eveli.permission.client.api.model.RoleCommand.ChangeRoleName;
import io.digiexpress.eveli.permission.client.api.model.RoleCommand.ChangeRoleParent;
import io.digiexpress.eveli.permission.client.api.model.RoleCommand.ChangeRolePermissions;
import io.digiexpress.eveli.permission.client.api.model.RoleCommand.ChangeRolePrincipals;
import io.digiexpress.eveli.permission.client.api.model.RoleCommand.ChangeRoleStatus;
import io.digiexpress.eveli.permission.client.api.model.RoleCommand.RoleUpdateCommand;
import io.resys.thena.api.actions.OrgCommitActions.ModType;
import io.resys.thena.api.actions.OrgCommitActions.ModifyOneParty;
import io.resys.thena.api.actions.OrgCommitActions.OnePartyEnvelope;
import io.resys.thena.api.entities.org.OrgActorStatusType;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.support.RepoAssert;
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
    final var ids = commands.stream().map(e -> e.getId()).distinct().toList();
    RepoAssert.isTrue(ids.size() == 1, () -> "Update commands must have same id because they are for the same role!");
    final var id = ids.get(0);
    
    return createRequest(id, commands).onItem().transform(resp -> createResponse(id, resp));
  }
  
  
  public Uni<OnePartyEnvelope> createRequest(String id, List<RoleUpdateCommand> commands) {
    final ModifyOneParty modifyOneParty = ctx.getOrg(ctx.getConfig().getRepoId()).commit().modifyOneParty();
    
    for (RoleUpdateCommand command : commands) {
      switch(command.getCommandType()) {
      
      case CHANGE_ROLE_DESCRIPTION: {
        final var description = (ChangeRoleDescription) command;
        modifyOneParty.partyDescription(description.getDescription());
        break;
      }
      
      case CHANGE_ROLE_NAME: {
        final var name = (ChangeRoleName) command;
        modifyOneParty.partyName(name.getName());
        break;
      }
      
      case CHANGE_ROLE_STATUS: {
        final var status = (ChangeRoleStatus) command;
        modifyOneParty.status(status.getStatus());
        break;
      }
      
      case CHANGE_ROLE_PARENT: {
        final var parent = (ChangeRoleParent) command;
        modifyOneParty.parentId(parent.getParentId());
        break;
      }
      
      case CHANGE_ROLE_PERMISSIONS: {
        final var permissions = (ChangeRolePermissions) command;
        
        if(permissions.getChangeType() == ChangeType.ADD) {
          permissions.getPermissions().forEach(perm -> modifyOneParty.modifyRight(ModType.ADD, perm));
          
        } else if(permissions.getChangeType() == ChangeType.REMOVE) {
          permissions.getPermissions().forEach(perm -> modifyOneParty.modifyRight(ModType.REMOVE, perm)); 
          
        } else if(permissions.getChangeType() == ChangeType.SET_ALL) {
          modifyOneParty.setAllRights(permissions.getPermissions());
          
        } else {
          throw new UpdateRoleException("Command type not found exception: " + command.getCommandType() + "/" + permissions.getChangeType());
        }
        break;
      }
      
      case CHANGE_ROLE_PRINCIPALS: {
        final var principals = (ChangeRolePrincipals) command;
        
        if(principals.getChangeType() == ChangeType.ADD) {
          principals.getPrincipals().forEach(principal -> modifyOneParty.modifyMember(ModType.ADD, principal));
        
        } else if(principals.getChangeType() == ChangeType.REMOVE) {
          principals.getPrincipals().forEach(principal -> modifyOneParty.modifyMember(ModType.REMOVE, principal));
        
        } else if(principals.getChangeType() == ChangeType.SET_ALL) {
          modifyOneParty.setAllMembers(principals.getPrincipals());
        
        } else {
          throw new UpdateRoleException("Command type not found exception: " + command.getCommandType() + "/" + principals.getChangeType());
        }
        break;
      }
      
      default: throw new UpdateRoleException("Command type not found exception ='%s'!".formatted(command.getCommandType())); 
      }
    }
    
    
    return modifyOneParty
      .partyId(id) 
      .message("Role update")
      .author(ctx.getConfig().getAuthor().get())
      .build();
  }
  
  
  public Role createResponse(String id, OnePartyEnvelope response) {
    if(response.getStatus() != CommitResultStatus.OK) {
      final var msg = "failed to update role by id ='%s'!".formatted(id);
      throw new UpdateRoleException(msg, response);
    }
    final var permissions = response.getDirectRights().stream().map(right -> right.getRightName()).toList();
    final var principals = response.getDirectMembers().stream().map(member -> member.getUserName()).toList();
    
    final var role = response.getParty();
    return ImmutableRole.builder()
      .id(role.getId())
      .parentId(role.getParentId())
      .version(role.getCommitId())
      
      .name(role.getPartyName())
      .description(role.getPartyDescription())
      .status(OrgActorStatusType.IN_FORCE)
      .permissions(permissions) 
      .principals(principals)
      .build();
    }
  
  
  public static class UpdateRoleException extends RuntimeException {
    private static final long serialVersionUID = 1224569524137159792L;
    
    public UpdateRoleException(String message, OnePartyEnvelope response) {
      super(message + System.lineSeparator() + "  " + 
          String.join(System.lineSeparator() + "  ", response.getMessages().stream().map(e -> e.getText()).toList()));
            response.getMessages().stream().filter(e -> e.getException()!= null).forEach(e -> {
            addSuppressed(e.getException());
      });
    }
    public UpdateRoleException(String message) {
      super(message);
    }
  }


  @Override
  public UpdateRoleAction evalAccess(RoleAccessEvaluator eval) {
    // TODO Auto-generated method stub
    return null;
  }
}
