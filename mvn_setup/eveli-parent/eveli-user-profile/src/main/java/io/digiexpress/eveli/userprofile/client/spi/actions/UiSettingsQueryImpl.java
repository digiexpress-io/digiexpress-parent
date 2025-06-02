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

import java.util.List;

import io.digiexpress.eveli.userprofile.client.api.UserProfileClient.UiSettingsQuery;
import io.digiexpress.eveli.userprofile.client.api.model.UiSettings;
import io.digiexpress.eveli.userprofile.client.spi.UserProfileStore;
import io.digiexpress.eveli.userprofile.client.spi.visitors.FindAllUserUiSettingsVisitor;
import io.digiexpress.eveli.userprofile.client.spi.visitors.GetUserUiSettingsVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class UiSettingsQueryImpl implements UiSettingsQuery {
  private final UserProfileStore ctx;
  
  @Override
  public Uni<List<UiSettings>> findAll(String profileId) {
    return ctx.getConfig().accept(new FindAllUserUiSettingsVisitor(profileId));
  }
  
  @Override
  public Uni<UiSettings> get(String profileId, String settingsId) {
    return ctx.getConfig().accept(new GetUserUiSettingsVisitor(profileId, settingsId));
  }

}
