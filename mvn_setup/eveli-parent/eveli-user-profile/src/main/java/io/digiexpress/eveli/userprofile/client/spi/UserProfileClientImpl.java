package io.digiexpress.eveli.userprofile.client.spi;

/*-
 * #%L
 * eveli-user-profile
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

import java.util.Optional;

import io.digiexpress.eveli.userprofile.client.api.UserProfileClient;
import io.digiexpress.eveli.userprofile.client.spi.actions.CreateUserProfileActionImpl;
import io.digiexpress.eveli.userprofile.client.spi.actions.UiSettingsQueryImpl;
import io.digiexpress.eveli.userprofile.client.spi.actions.UpdateUiSettingsActionImpl;
import io.digiexpress.eveli.userprofile.client.spi.actions.UpdateUserProfileActionImpl;
import io.digiexpress.eveli.userprofile.client.spi.actions.UserProfileQueryImpl;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserProfileClientImpl implements UserProfileClient {
  private final UserProfileStore ctx;
  
  public UserProfileStore getCtx() { return ctx; }
  
  @Override
  public UserProfileClient withRepoId(String repoId) {
    return new UserProfileClientImpl(ctx.withTenantId(repoId));
  }

  @Override
  public Uni<Tenant> getRepo() {
    return ctx.getTenant();
  }
  
  @Override
  public CreateUserProfileAction createUserProfile(){
    return new CreateUserProfileActionImpl(ctx);
  }
  @Override
  public UpdateUserProfileAction updateUserProfile() {
    return new UpdateUserProfileActionImpl(ctx);
  }
  @Override
  public UserProfileQuery userProfileQuery() {
    return new UserProfileQueryImpl(ctx);
  }
  @Override
  public UiSettingsQuery uiSettingsQuery() {
    return new UiSettingsQueryImpl(ctx);
  }
  @Override
  public UpdateUiSettingsAction updateUiSettings() {
    return new UpdateUiSettingsActionImpl(ctx);
  }
  @Override
  public RepositoryQuery repoQuery() {
    var repo = ctx.query();
    return new RepositoryQuery() {
      private String repoName;
      
      @Override public Uni<UserProfileClient> createIfNot() { return repo.createIfNot().onItem().transform(doc -> new UserProfileClientImpl(doc)); }
      @Override public Uni<UserProfileClient> create() { return repo.create().onItem().transform(doc -> new UserProfileClientImpl(doc)); }
      @Override public UserProfileClient build() { return new UserProfileClientImpl(repo.build()); }
      @Override public Uni<UserProfileClient> delete() { return repo.delete().onItem().transform(doc -> new UserProfileClientImpl(doc)); }
      @Override public Uni<UserProfileClient> deleteAll() { return repo.deleteAll().onItem().transform(doc -> new UserProfileClientImpl(ctx)); }
      @Override
      public RepositoryQuery repoName(String repoName) {
        this.repoName = repoName;
        repo.repoName(repoName);
        return this;
      }
      @Override
      public Uni<Optional<UserProfileClient>> get() {
        RepoAssert.notEmpty(repoName, () -> "repoName must be defined!");
        
        final var client = ctx.getConfig().getClient();
        return client.tenants().find().id(repoName)
            .get().onItem().transform(existing -> {
              if(existing == null) {
                final Optional<UserProfileClient> result = Optional.empty();
                return result;
              }
              return Optional.of(new UserProfileClientImpl(repo.build()));
            });
        
      }
    };
  }

}
