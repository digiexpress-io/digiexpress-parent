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

import java.util.List;

import io.digiexpress.eveli.permission.client.api.PermissionClient.PermissionAccessEvaluator;
import io.digiexpress.eveli.permission.client.api.PermissionClient.PermissionQuery;
import io.digiexpress.eveli.permission.client.api.model.ImmutablePermission;
import io.digiexpress.eveli.permission.client.api.model.Principal.Permission;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgRightHierarchy;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PermissionQueryImpl implements PermissionQuery {

  private final PermissionStore ctx;
  @Override
  public Uni<Permission> get(String permissionId) {
    final var repoId = ctx.getConfig().getRepoId();
    final Uni<QueryEnvelope<OrgRightHierarchy>> permission = ctx.getOrg(repoId).find().rightHierarchyQuery().get(permissionId);
    
    return permission.onItem().transform((response) -> {
      if(response.getStatus() != QueryEnvelopeStatus.OK) {
        final var msg = "failed to get permission by id = '%s'!".formatted(permissionId);
        final var exception = new PermissionQueryException(msg);
        
        response.getMessages()
          .forEach((e) -> {
            if(e.getException() != null) {
              exception.addSuppressed(e.getException());
            }
          });
        log.error(msg);
        throw exception;
      }
      return mapTo(response.getObjects());
    });
  }

  @Override
  public Uni<List<Permission>> findAllPermissions() {
    final var repoId = ctx.getConfig().getRepoId();
    final Uni<QueryEnvelopeList<OrgRightHierarchy>> permissions = ctx.getOrg(repoId).find().rightHierarchyQuery().findAll();
    
    return permissions.onItem().transform((response) -> {
      if(response.getStatus() != QueryEnvelopeStatus.OK) {
        final var msg = "failed to find all permissions!";
        final var exception = new PermissionQueryException(msg);
        
        response.getMessages()
          .forEach((e) -> {
            if(e.getException() != null) {
            exception.addSuppressed(e.getException());
          }
        });
        log.error(msg);
        throw exception;
      }
      return response.getObjects().stream().map(this::mapTo).toList();
    });
  }
  
  
  private Permission mapTo(OrgRightHierarchy permission) {
    final var directRoles = permission.getDirectParty().stream().map((role -> role.getPartyName())).toList();
    final var directPrincipals = permission.getDirectMembers().stream().map((member -> member.getUserName())).toList();
    
    final var inheritedRoles = permission.getChildParty().stream().map((role -> role.getPartyName())).toList();
    final var inheritedPrincipals = permission.getChildMembers().stream().map((member -> member.getUserName())).toList();
    
    
    return ImmutablePermission.builder()
      .id(permission.getRight().getId())
      .version(permission.getRight().getCommitId())
      .name(permission.getRight().getRightName())
      .description(permission.getRight().getRightDescription())
      .status(permission.getRight().getStatus())
      .addAllRoles(directRoles)
      .addAllPrincipals(directPrincipals)
      
      .addAllRoles(inheritedRoles)
      .addAllPrincipals(inheritedPrincipals)
      
      
      .build();
  }

  
  public static class PermissionQueryException extends RuntimeException {
    private static final long serialVersionUID = 4727517899929638306L;

    public PermissionQueryException(String message) {
      super(message);
    }
  }


  @Override
  public PermissionQuery evalAccess(PermissionAccessEvaluator eval) {
    // TODO Auto-generated method stub
    return null;
  }
}
