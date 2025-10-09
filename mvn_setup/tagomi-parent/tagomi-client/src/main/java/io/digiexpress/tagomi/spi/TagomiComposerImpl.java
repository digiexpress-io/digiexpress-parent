package io.digiexpress.tagomi.spi;

/*-
 * #%L
 * tagomi-client
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

import io.digiexpress.tagomi.api.TagomiComposer;
import io.digiexpress.tagomi.api.TagomiImageStorage;
import io.digiexpress.tagomi.api.TagomiStore;
import io.digiexpress.tagomi.api.commands.TagomiCreateCommands;
import io.digiexpress.tagomi.api.commands.TagomiDeleteCommands;
import io.digiexpress.tagomi.api.commands.TagomiUpdateCommands;
import io.digiexpress.tagomi.spi.commands.TagomiCreateCommandsImpl;
import io.digiexpress.tagomi.spi.commands.TagomiDeleteCommandsImpl;
import io.digiexpress.tagomi.spi.commands.TagomiUpdateCommandsImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TagomiComposerImpl implements TagomiComposer {
  private final TagomiStore store;
  private final TagomiImageStorage imageStorage;
  @Override
  public TagomiCreateCommands create() {
    return new TagomiCreateCommandsImpl(store, imageStorage);
  }
  @Override
  public TagomiUpdateCommands update() {
    return new TagomiUpdateCommandsImpl(store, imageStorage);
  }
  @Override
  public TagomiDeleteCommands delete() {
    return new TagomiDeleteCommandsImpl(store);
  }
  @Override
  public TagomiStore unwrap() {
    return store;
  }
}
