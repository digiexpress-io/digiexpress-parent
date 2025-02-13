package io.resys.userprofile.client.spi.actions;

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

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import io.resys.userprofile.client.api.UserProfileClient.UserProfileQuery;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.spi.UserProfileStore;
import io.resys.userprofile.client.spi.visitors.DeleteAllUserProfilesVisitor;
import io.resys.userprofile.client.spi.visitors.FindAllUserProfilesVisitor;
import io.resys.userprofile.client.spi.visitors.GetActiveUserProfileVisitor;
import io.resys.userprofile.client.spi.visitors.GetUserProfilesByIdsVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class UserProfileQueryImpl implements UserProfileQuery {
  private final UserProfileStore ctx;
  
  @Override
  public Uni<UserProfile> get(String profileId) {
    return ctx.getConfig().accept(new GetActiveUserProfileVisitor(profileId));
  }
  
  @Override
  public Uni<List<UserProfile>> findAll() {
    return ctx.getConfig().accept(new FindAllUserProfilesVisitor());
  }

  @Override
  public Uni<List<UserProfile>> deleteAll(String userId, Instant targetDate) {
    return ctx.getConfig().accept(new DeleteAllUserProfilesVisitor())
        .onItem().transformToUni(unwrap -> unwrap);
  }
  
  @Override
  public Uni<List<UserProfile>> findByIds(Collection<String> profileIds) {
    return ctx.getConfig().accept(new GetUserProfilesByIdsVisitor(profileIds));
  }
}
