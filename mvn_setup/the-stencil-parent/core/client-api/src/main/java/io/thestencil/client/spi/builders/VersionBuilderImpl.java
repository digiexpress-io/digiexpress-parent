package io.thestencil.client.spi.builders;

/*-
 * #%L
 * stencil-client-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import io.thestencil.client.api.ImmutableVersionInfo;
import io.thestencil.client.api.VersionBuilder;
import io.thestencil.client.api.StencilClient.VersionInfo;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class VersionBuilderImpl implements VersionBuilder {

  private static final String VERSION = "1.148.30";
  private static final String DATE = "04/07/2023";

  @Override
  public VersionInfo version() {
    return ImmutableVersionInfo.builder()
        .version(VERSION)
        .date(DATE)
        .build();
  }

}


