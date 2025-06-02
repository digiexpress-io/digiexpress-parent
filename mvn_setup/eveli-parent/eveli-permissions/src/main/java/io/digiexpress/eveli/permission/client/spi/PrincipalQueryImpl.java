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

import io.digiexpress.eveli.permission.client.api.PermissionClient.PrincipalAccessEvaluator;
import io.digiexpress.eveli.permission.client.api.PermissionClient.PrincipalNotFoundException;
import io.digiexpress.eveli.permission.client.api.PermissionClient.PrincipalQuery;
import io.digiexpress.eveli.permission.client.api.model.ImmutablePrincipal;
import io.digiexpress.eveli.permission.client.api.model.Principal;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgMemberHierarchy;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PrincipalQueryImpl implements PrincipalQuery {
  private final PermissionStore ctx;
  
  @Override
  public Uni<Principal> get(String principalId) {
    final var repoId = ctx.getConfig().getRepoId();
    final Uni<QueryEnvelope<OrgMemberHierarchy>> user = ctx.getOrg(repoId).find().memberHierarchyQuery().get(principalId);
    return user.onItem().transform((response) -> {
      if(response.isNotFound()) {
        throw new PrincipalNotFoundException("Can't find principal by id: " + principalId + "!");
      }
      
      if(response.getStatus() != QueryEnvelopeStatus.OK) {
        final var msg = "Failed to get principal by id = '%s'!".formatted(principalId);
        final var exception = new PrincipalQueryException(msg);
        
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
  public Uni<List<Principal>> findAllPrincipals() {
    final var repoId = ctx.getConfig().getRepoId();
    final Uni<QueryEnvelopeList<OrgMemberHierarchy>> users = ctx.getOrg(repoId).find().memberHierarchyQuery().findAll();
    return users.onItem().transform((response) -> {
      if(response.getStatus() != QueryEnvelopeStatus.OK) {
        final var msg = "Failed to find all principals!";
        final var exception = new PrincipalQueryException(msg);
        
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
  
  private Principal mapTo(OrgMemberHierarchy user) {
        
    return ImmutablePrincipal.builder()
        .id(user.getMember().getId())
        .version(user.getMember().getCommitId()) //TODO
        .name(user.getMember().getUserName())
        .email(user.getMember().getEmail())
        .status(user.getMember().getStatus())
        
        .addAllDirectPermissions(user.getDirectRightNames())
        .addAllDirectRoles(user.getDirectPartyNames())
        
        .addAllRoles(user.getPartyNames())
        .addAllPermissions(user.getRightNames())
        .build();
  }

  public static class PrincipalQueryException extends RuntimeException {

    private static final long serialVersionUID = 4727517899929638306L;

    public PrincipalQueryException(String message) {
      super(message);
    }
    
  }

  @Override
  public PrincipalQuery evalAccess(PrincipalAccessEvaluator eval) {
    // TODO Auto-generated method stub
    return null;
  }
  
}
