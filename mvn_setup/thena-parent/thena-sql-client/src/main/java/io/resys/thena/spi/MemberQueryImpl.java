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

import io.resys.thena.api.actions.TenantActions.MemberQuery;
import io.resys.thena.api.entities.Member;
import io.smallrye.mutiny.Multi;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class MemberQueryImpl implements MemberQuery {
  private final TenantDataSource state;

  private String externalId;
  private String refTenant;
  private UUID aliasId;
  
  @Override
  public MemberQuery externalId(String externalId) {
    this.externalId = externalId;
    return this;
  }
  @Override
  public MemberQuery refTenant(String refTenant) {
    this.refTenant = refTenant;
    return this;
  }

  @Override
  public MemberQuery aliasId(UUID aliasId) {
    this.aliasId = aliasId;
    return this;
  }

  @Override
  public Multi<Member> findAll() {
    return state.member().findByExtIdAndAliasIdAndRef(externalId, aliasId, refTenant);
  }
}
