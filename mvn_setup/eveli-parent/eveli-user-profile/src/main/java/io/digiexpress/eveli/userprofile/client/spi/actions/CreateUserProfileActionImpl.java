package io.digiexpress.eveli.userprofile.client.spi.actions;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.digiexpress.eveli.userprofile.client.api.UserProfileClient.CreateUserProfileAction;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.UpsertUserProfile;
import io.digiexpress.eveli.userprofile.client.spi.UserProfileStore;
import io.digiexpress.eveli.userprofile.client.spi.visitors.CreateUserProfileVisitor;
import io.digiexpress.eveli.userprofile.client.spi.visitors.UpdateUserProfileVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateUserProfileActionImpl implements CreateUserProfileAction {
  private final UserProfileStore ctx;
  
  @Override
  public Uni<UserProfile> createOne(CreateUserProfile command) {
    return this.createMany(Arrays.asList(command))
        .onItem().transform(items -> items.get(0)) ;
  }
  
  @Override
  public Uni<UserProfile> createOne(UpsertUserProfile command) {
    return ctx.getConfig().accept(new UpdateUserProfileVisitor(Arrays.asList(command), ctx)).onItem()
        .transformToUni(item -> item).onItem().transform(items -> items.get(0));
  }

  @Override
  public Uni<List<UserProfile>> createMany(List<? extends CreateUserProfile> commands) {
    return ctx.getConfig().accept(new CreateUserProfileVisitor(commands));
  }

  @Override
  public Uni<List<UserProfile>> upsertMany(List<? extends UpsertUserProfile> commands) {
    return ctx.getConfig().accept(new UpdateUserProfileVisitor(new ArrayList<>(commands), ctx)).onItem()
        .transformToUni(item -> item).onItem().transform(items -> items);
  }



}
