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

import io.digiexpress.eveli.permission.client.api.PermissionClient.CreatePermissionAction;
import io.digiexpress.eveli.permission.client.api.PermissionClient.PermissionAccessEvaluator;
import io.digiexpress.eveli.permission.client.api.model.ImmutablePermission;
import io.digiexpress.eveli.permission.client.api.model.PermissionCommand.CreatePermission;
import io.digiexpress.eveli.permission.client.api.model.PermissionCommand.PermissionCommandType;
import io.digiexpress.eveli.permission.client.api.model.Principal.Permission;
import io.resys.thena.api.actions.OrgCommitActions.CreateOneRight;
import io.resys.thena.api.actions.OrgCommitActions.OneRightEnvelope;
import io.resys.thena.api.entities.org.OrgActorStatusType;
import io.resys.thena.api.envelope.CommitResultStatus;
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
          .addRightToMembers(permission.getPrincipals())
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
        .status(OrgActorStatusType.IN_FORCE)
        .description(permission.getRightDescription())
        .name(permission.getRightName())
        .roles(response.getDirectParties().stream().map(party -> party.getPartyName()).toList())
        .principals(response.getDirectMembers().stream().map(member -> member.getUserName()).toList())
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

  @Override
  public CreatePermissionAction evalAccess(PermissionAccessEvaluator eval) {
    // TODO Auto-generated method stub
    return null;
  }
}
