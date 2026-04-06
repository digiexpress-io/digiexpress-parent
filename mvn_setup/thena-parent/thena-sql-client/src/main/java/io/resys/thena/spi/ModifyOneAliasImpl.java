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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import io.resys.thena.api.actions.TenantActions.ModifyOneAlias;
import io.resys.thena.api.entities.Alias;
import io.resys.thena.api.entities.Alias.AliasConfig;
import io.resys.thena.api.entities.ImmutableAlias;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyOneAliasImpl implements ModifyOneAlias {
  private final TenantDataSource state;
  private String author;
  private String desc;
  private String aliasId;
  private final List<AliasConfig> config = new ArrayList<>();
  
  @Override
  public ModifyOneAlias aliasId(String aliasId) {
    this.aliasId = RepoAssert.notEmpty(aliasId, () -> "aliasId can't be empty!");
    return this;
  }
  @Override
  public ModifyOneAlias author(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!");
    return this;
  }
  @Override
  public ModifyOneAlias aliasDesc(String aliasDesc) {
    this.desc = RepoAssert.notEmpty(aliasDesc, () -> "aliasDesc can't be empty!");
    return this;
  }
  @Override
  public ModifyOneAlias aliasConfig(List<AliasConfig> config) {
    this.config.addAll(config);
    return this;
  }

  @Override
  public Uni<Alias> build() {
    RepoAssert.notEmpty(this.aliasId, () -> "aliasId can't be empty!");
    RepoAssert.notEmpty(this.author, () -> "author can't be empty!");
        
    return state.alias().findAll().collect().asList()
        .onItem().transformToUni((aliass) -> {
          
          final var existing = aliass.stream().filter(c -> 
            c.getId().toString().equals(this.aliasId) ||
            c.getAliasName().equals(this.aliasId)).findFirst()
          .orElseThrow(() -> new ModifyOneAliasException("Can't find alias by id or name: '" + this.aliasId + "'"));
          
      final var now = OffsetDateTime.now();

      final var merged = ImmutableAlias.builder()
          .from(existing)
          .updatedAt(now).updatedBy(author)
          .aliasDesc(desc == null ? existing.getAliasDesc() : desc)
          .aliasConfig(config.isEmpty() ? existing.getAliasConfig() : config)
          .build();
      return state.alias().merge(merged);
    });
  }

  public static class ModifyOneAliasException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    
    public ModifyOneAliasException(String message) {
      super(message);
    }
  }
}
