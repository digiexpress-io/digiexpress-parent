package io.digiexpress.eveli.client.config;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.util.function.Supplier;

import io.digiexpress.eveli.assets.api.EveliAssetClient;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.api.StencilClient;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EveliContext {

  private final StencilClient stencil;
  private final HdesClient wrench;
  private final EveliAssetClient assets;
  private final Supplier<ProgramEnvir> programEnvir;
  private final Supplier<Sites> siteEnvir;
}
