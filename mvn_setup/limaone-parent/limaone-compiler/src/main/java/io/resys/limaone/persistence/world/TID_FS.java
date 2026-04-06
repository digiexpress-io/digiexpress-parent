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

import java.util.UUID;

import io.resys.limaone.authoring.NewAliasImpl;
import io.resys.limaone.authoring.TID;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.thena.api.actions.TenantActions.MemberQuery;
import io.resys.thena.api.actions.TenantActions.TenantAware;
import io.resys.thena.api.entities.Alias;
import io.resys.thena.api.entities.Member;
import io.smallrye.mutiny.Multi;



public class TID_FS implements TID {
  private final TenantAware<?> client;
  private final AuthoringConfig config;

  public TID_FS(AuthoringConfig config) {
    super();
    this.client = ((TenantAware<?>) config.getPersistence());
    this.config = config;
  }
  
  @Override
  public AliasQuery aliasQuery() {
    return new AliasQuery() {
      @Override
      public Multi<Alias> findAll() {
        return client.getActions().queryAliases().findAll();
      }
    };
  }

  @Override
  public ModifyAlias modifyAlias() {
    return new ModifyAliasImpl(client, config);
  }
  @Override
  public NewAlias newAlias() {
    return new NewAliasImpl(client, config);
  }
  @Override
  public UpsertMember upsertMember() {
    return new UpsertMemberImpl(client, config);
  }

  @Override
  public MemberQuery memberQuery() {
    final var currentUser = config.getEnvir().getCurrentUser().get();
   
    final var externalId = currentUser.getUserName();
    final var delegate = client.getActions().queryMembers().externalId(externalId);
    
    return new MemberQuery() {
      @Override
      public MemberQuery refTenant(String refTenant) {
        delegate.refTenant(refTenant);
        return this;
      }
      @Override
      public MemberQuery externalId(String externalId) {
        delegate.externalId(externalId);
        return this;
      }
      @Override
      public MemberQuery aliasId(UUID aliasId) {
        delegate.aliasId(aliasId);
        return this;
      }
      @Override
      public Multi<Member> findAll() {
        return delegate.findAll();
      }
      
    };
  }
}
