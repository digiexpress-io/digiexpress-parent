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

import io.digiexpress.eveli.userprofile.client.api.UserProfileClient.UpdateUiSettingsAction;
import io.digiexpress.eveli.userprofile.client.api.model.UiSettings;
import io.digiexpress.eveli.userprofile.client.api.model.UiSettingsCommand.UiSettingsUpdateCommand;
import io.digiexpress.eveli.userprofile.client.spi.UserProfileStore;
import io.digiexpress.eveli.userprofile.client.spi.visitors.UpdateUiSettingsVisitor;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateUiSettingsActionImpl implements UpdateUiSettingsAction {

  private final UserProfileStore ctx;

  @Override
  public Uni<UiSettings> updateOne(UiSettingsUpdateCommand command) {
    RepoAssert.notNull(command, () -> "No commands to apply!");
    
    return ctx.getConfig().accept(new UpdateUiSettingsVisitor(command, ctx))
        .onItem().transformToUni(resp -> resp)
        .onItem().transform(customers -> customers);
  }
}

