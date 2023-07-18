package io.resys.hdes.client.spi.web;

/*-
 * #%L
 * hdes-client
 * %%
 * Copyright (C) 2020 - 2022 Copyright 2020 ReSys OÜ
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

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesComposer;

public class HdesWebContext {
  private final HdesComposer composer;
  private final HdesClient client;
  private final HdesWebConfig config;

  public HdesWebContext(HdesComposer composer, HdesClient client, HdesWebConfig config) {
    super();
    this.client = client;
    this.config = config;
    this.composer = composer;
  }

  public HdesClient getClient() {
    return client;
  }

  public HdesWebConfig getConfig() {
    return config;
  }

  public HdesComposer getComposer() {
    return composer;
  }

}
