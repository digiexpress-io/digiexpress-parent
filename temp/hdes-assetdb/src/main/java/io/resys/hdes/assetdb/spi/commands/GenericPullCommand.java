package io.resys.hdes.assetdb.spi.commands;

/*-
 * #%L
 * hdes-assetdb
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import io.resys.hdes.assetdb.api.AssetClient.Objects;
import io.resys.hdes.assetdb.api.AssetCommands.PullCommand;

public abstract class GenericPullCommand implements PullCommand {

  private final Objects objects;
  
  public GenericPullCommand(Objects objects) {
    super();
    this.objects = objects;
  }
  
  @Override
  public Objects build() {
    Objects newState = fetch();  
    return newState;
  }
  
  protected abstract Objects fetch();
}
