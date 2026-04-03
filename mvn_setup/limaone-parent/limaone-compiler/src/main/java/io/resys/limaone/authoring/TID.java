package io.resys.limaone.authoring;

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

import java.util.List;
import java.util.UUID;

import org.immutables.value.Value;

import io.resys.limaone.program.Runtime.CurrentUser;
import io.resys.thena.api.actions.TenantActions.MemberQuery;
import io.resys.thena.api.entities.Alias;
import io.resys.thena.api.entities.Alias.AliasConfig;
import io.resys.thena.api.entities.Member;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface TID {

  AliasQuery aliasQuery();
  NewAlias newAlias();
  ModifyAlias modifyAlias();
  UpsertMember upsertMember();
  MemberQuery memberQuery();
  
  
  interface AliasQuery {
    Multi<Alias> findAll();
  }
  
  interface NewAlias {
    // new alias name
    NewAlias aliasName(String aliasName);
    
    // some user description
    NewAlias aliasDesc(String aliasDesc);
    
    NewAlias aliasConfig(List<AliasConfig> aliasConfig);
    
    Uni<Alias> build();
  }


  interface UpsertMember {
    // will resolve on it own 
    UpsertMember user(@Nullable CurrentUser user); 
    UpsertMember aliasId(UUID aliasId);
    
    // true enabled
    UpsertMember aliasStatus(boolean aliasStatus);
    
    Uni<Member> build();
  }

  
  interface ModifyAlias {
    ModifyAlias aliasId(UUID aliasId);
    ModifyAlias aliasDesc(String aliasDesc);
    ModifyAlias aliasConfig(List<AliasConfig> aliasConfig);
    Uni<Alias> build();
  }
  
  @Value.Immutable
  interface AliasMember {
    Alias getAlias();
    Boolean getAliasStatus();
  }
}
