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
import java.util.UUID;

import io.resys.thena.api.actions.TenantActions.CreateOneAlias;
import io.resys.thena.api.entities.Alias;
import io.resys.thena.api.entities.Alias.AliasConfig;
import io.resys.thena.api.entities.ImmutableAlias;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateOneAliasImpl implements CreateOneAlias {

  private final TenantDataSource state;
  
  private String author;
  private String refTenantId;
  private String aliasTenantId;
  private String name;
  private String desc;
  private final List<AliasConfig> config = new ArrayList<>();
  
  @Override
  public CreateOneAliasImpl author(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  @Override
  public CreateOneAlias refTenantId(String refTenantId) {
    this.refTenantId = RepoAssert.notEmpty(refTenantId, () -> "refTenantId can't be empty!"); 
    return this;
  }
  @Override
  public CreateOneAlias aliasTenantId(String aliasTenantId) {
    this.aliasTenantId = RepoAssert.notEmpty(aliasTenantId, () -> "aliasTenantId can't be empty!"); 
    return this;
  }
  @Override
  public CreateOneAlias aliasName(String aliasName) {
    this.name = RepoAssert.notEmpty(aliasName, () -> "aliasName can't be empty!");
    return this;
  }
  @Override
  public CreateOneAlias aliasDesc(String aliasDesc) {
    this.desc = RepoAssert.notEmpty(aliasDesc, () -> "aliasDesc can't be empty!");
    return this;
  }
  @Override
  public CreateOneAliasImpl aliasConfig(List<AliasConfig> config) {
    RepoAssert.notNull(config, () -> "config can't be empty!");
    this.config.addAll(config);
    return this;
  }

  @Override
  public Uni<Alias> build() {
    RepoAssert.isName(name, () -> "aliasName can't be empty!");
    RepoAssert.notEmpty(refTenantId, () -> "tenantName can't be empty!");
    RepoAssert.notEmpty(aliasTenantId, () -> "aliasTenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");

    return Uni.combine().all()
        .unis(
            state.alias().findAll().collect().asList(),
            state.tenant().findAll().collect().asList())
        .asTuple()
        .onItem().transformToUni((tuple) -> {
          
      final var aliasName = this.name.toLowerCase();
      final var isDup = tuple.getItem1().stream().filter(e -> e.getAliasName().equals(aliasName)).count() > 0;
      if(isDup) {
        throw new CreateOneAliasException("Alias with similar name exists!");
      }
      
      final var refTenant = tuple.getItem2().stream()
        .filter(t -> t.getName().equals(refTenantId) || t.getId().equals(refTenantId))
        .findFirst().orElseThrow(() -> new CreateOneAliasException("Can't find tenant with name or id: '" + refTenantId + "'!"));

      final var aliasTenant = tuple.getItem2().stream()
          .filter(t -> t.getName().equals(aliasTenantId) || t.getId().equals(aliasTenantId))
          .findFirst().orElseThrow(() -> new CreateOneAliasException("Can't find tenant with name or id: '" + aliasTenantId + "'!"));
      
      final var now = OffsetDateTime.now();

      final var newRepo = ImmutableAlias.builder()
          .id(UUID.randomUUID())
          
          .refTenantId(refTenant.getId())
          .aliasTenantId(aliasTenant.getId())
          
          .createdAt(now).createdBy(author)
          .updatedAt(now).updatedBy(author)
          
          .aliasName(aliasName).aliasDesc(desc)
          .aliasConfig(config)
          .build();
      
      return state.alias().insert(newRepo);
    });
  }
  
  public static class CreateOneAliasException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    
    public CreateOneAliasException(String message) {
      super(message);
    }
  }
}
