package io.resys.userprofile.client.spi.actions;

import java.util.Arrays;

/*-
 * #%L
 * thena-tasks-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import io.resys.thena.support.RepoAssert;
import io.resys.userprofile.client.api.UserProfileClient.UpdateUserProfileAction;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.UserProfileUpdateCommand;
import io.resys.userprofile.client.spi.store.UserProfileStore;
import io.resys.userprofile.client.spi.visitors.UpdateUserProfileVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateUserProfileActionImpl implements UpdateUserProfileAction {

  private final UserProfileStore ctx;

  @Override
  public Uni<UserProfile> updateOne(UserProfileUpdateCommand command) {        
    return updateOne(Arrays.asList(command));
  }

  @Override
  public Uni<UserProfile> updateOne(List<UserProfileUpdateCommand> commands) {
    RepoAssert.notNull(commands, () -> "commands must be defined!");
    RepoAssert.isTrue(commands.size() > 0, () -> "No commands to apply!");
    
    
    return ctx.getConfig().accept(new UpdateUserProfileVisitor(commands, ctx))
        .onItem().transformToUni(resp -> resp)
        .onItem().transform(customers -> customers.get(0));
  }

  @Override
  public Uni<List<UserProfile>> updateMany(List<UserProfileUpdateCommand> commands) {
    RepoAssert.notNull(commands, () -> "commands must be defined!");
    RepoAssert.isTrue(commands.size() > 0, () -> "No commands to apply!");
    
    return ctx.getConfig().accept(new UpdateUserProfileVisitor(commands, ctx)).onItem()
        .transformToUni(item -> item);
  }
}

