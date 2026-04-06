package io.resys.limaone.persistence.world;

/*-
 * #%L
 * limaone-compiler
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

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import io.resys.limaone.authoring.TID.UpsertMember;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.program.Runtime.CurrentUser;
import io.resys.thena.api.actions.TenantActions.TenantAware;
import io.resys.thena.api.entities.Alias;
import io.resys.thena.api.entities.Member;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpsertMemberImpl implements UpsertMember {
  private final TenantAware<?> client;
  private final AuthoringConfig config;
  
  private CurrentUser user;
  private UUID aliasId;
  private Boolean aliasStatus;
  
  @Override
  public UpsertMember user(CurrentUser user) {
    this.user = user;
    return this;
  }
  @Override
  public UpsertMember aliasId(UUID aliasId) {
    this.aliasId = Objects.requireNonNull(aliasId, () -> "aliasId must be defined!");
    return this;
  }
  @Override
  public UpsertMember aliasStatus(boolean aliasStatus) {
    this.aliasStatus = aliasStatus;
    return this;
  }
  @Override
  public Uni<Member> build() {
    Objects.requireNonNull(aliasId, () -> "aliasId must be defined!");
    Objects.requireNonNull(aliasStatus, () -> "aliasStatus must be defined!");
    
    final var currentUser = Optional.ofNullable(user).orElseGet(config.getEnvir().getCurrentUser());
    final var externalId = currentUser.getUserName();
    
    final var member_uni = client.getActions().queryMembers()
      .externalId(externalId)
      .findAll().collect().asList();
    
    final var alias_uni = client.getActions().queryAliases().id(aliasId).getOne();

    
    return Uni.combine().all().unis(member_uni, alias_uni).asTuple()
        .onItem().transformToUni(tuple -> {
          if(tuple.getItem1().isEmpty()) {
            return createMember(tuple.getItem2(), externalId);
          }
          return updateMember(tuple.getItem2(), tuple.getItem1().getFirst());
        });
  }

  private Uni<Member> updateMember(Alias alias, Member previous) {
    if(previous.getAliasId().equals(aliasId) && previous.getAliasStatus().equals(aliasStatus)) {
      return Uni.createFrom().item(previous);
    }
    
    return client.getActions().modifyOneMember()
        .aliasId(aliasId)
        .aliasStatus(aliasStatus)
        .memberId(previous.getId())
        .build();
  }
  
  private Uni<Member> createMember(Alias alias, String externalId) {
    return client.getActions().createOneMember()
      .aliasId(alias.getId())
      .aliasStatus(aliasStatus)
      .externalId(externalId)
      .build();
  }
}
