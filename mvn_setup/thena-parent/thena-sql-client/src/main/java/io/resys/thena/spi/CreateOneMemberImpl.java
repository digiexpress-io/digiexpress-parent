package io.resys.thena.spi;

/*-
 * #%L
 * thena-sql-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.UUID;

import io.resys.thena.api.actions.TenantActions.CreateOneMember;
import io.resys.thena.api.entities.ImmutableMember;
import io.resys.thena.api.entities.Member;
import io.resys.thena.spi.CreateOneAliasImpl.CreateOneAliasException;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateOneMemberImpl implements CreateOneMember {
  private final TenantDataSource state;
  
  private String externalId;
  private Boolean aliasStatus;
  private UUID aliasId;
  
  @Override
  public CreateOneMember externalId(String externalId) {
    this.externalId = RepoAssert.notEmpty(externalId, () -> "externalId can't be empty!"); 
    return this;
  }

  @Override
  public CreateOneMember aliasId(UUID aliasId) {
    this.aliasId = aliasId == null ? null : aliasId; 
    return this;
  }
  
  @Override
  public CreateOneMember aliasStatus(Boolean aliasStatus) {
    this.aliasStatus = RepoAssert.notNull(aliasStatus, () -> "aliasStatus can't be empty!"); 
    return this;
  }
  
  @Override
  public Uni<Member> build() {
    RepoAssert.notEmpty(externalId, () -> "externalId can't be empty!");
    RepoAssert.notNull(aliasId, () -> "aliasId can't be empty!");
    RepoAssert.notNull(aliasStatus, () -> "aliasStatus can't be empty!");
    
    return state.member().findByExtIdAndAliasId(externalId, aliasId)
        .onItem()
        .transformToUni((members) -> {

      if(members.isPresent()) {
        throw new CreateOneAliasException("Member with similar name exists!");
      }

      final var newRepo = ImmutableMember.builder()
          .id(UUID.randomUUID())
          .externalId(externalId)
          .aliasId(aliasId)
          .aliasStatus(aliasStatus)
          .build();
      
      return state.member().insert(newRepo);
    });
  }

  
  
  public static class CreateOneMemberException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    
    public CreateOneMemberException(String message) {
      super(message);
    }
  }
}
