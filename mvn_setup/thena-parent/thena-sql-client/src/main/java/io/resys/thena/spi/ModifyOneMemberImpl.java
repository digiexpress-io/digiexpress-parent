package io.resys.thena.spi;

import java.util.Optional;

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

import io.resys.thena.api.actions.TenantActions.ModifyOneMember;
import io.resys.thena.api.entities.ImmutableMember;
import io.resys.thena.api.entities.Member;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyOneMemberImpl implements ModifyOneMember {
  private final TenantDataSource state;

  private Boolean aliasStatus;
  private UUID memberId;
  private UUID aliasId;

  @Override
  public ModifyOneMember aliasStatus(Boolean aliasStatus) {
    this.aliasStatus = RepoAssert.notNull(aliasStatus, () -> "aliasStatus can't be empty!");
    return this;
  }

  @Override
  public ModifyOneMemberImpl memberId(UUID memberId) {
    this.memberId = RepoAssert.notNull(memberId, () -> "memberId can't be empty!");
    return this;
  }
  @Override
  public ModifyOneMemberImpl aliasId(UUID aliasId) {
    this.aliasId = RepoAssert.notNull(aliasId, () -> "aliasId can't be empty!");
    return this;
  }
  @Override
  public Uni<Member> build() {
    RepoAssert.notNull(this.memberId, () -> "memberId can't be empty!");
    RepoAssert.notNull(this.aliasStatus, () -> "aliasStatus can't be empty!");
        
    return state.member().findById(this.memberId)
        .onItem().transformToUni((member) -> {          
      final var existing = member.orElseThrow(() -> new ModifyOneMemberException("Can't find by id or external id: '" + this.memberId + "'"));

      final var merged = ImmutableMember.builder()
          .from(existing)
          .aliasId(Optional.ofNullable(aliasId).orElse(existing.getAliasId()))
          .aliasStatus(aliasStatus)
          .build();
      return state.member().merge(merged);
    });
  }

  public static class ModifyOneMemberException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    
    public ModifyOneMemberException(String message) {
      super(message);
    }
  }


}
