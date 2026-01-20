package io.digiexpress.eveli.app;

/*-
 * #%L
 * eveli-app
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.client.config.EveliAutoConfigAssets;
import io.digiexpress.eveli.client.config.EveliAutoConfigAssets.EveliEditEnvir;
import io.digiexpress.eveli.client.config.EveliProps;
import io.digiexpress.eveli.client.config.EveliPropsAssets;


@Profile("!prod")
@Configuration
public class AppConfigEditAssets {

  
  // Bean controlling that stencil/wrench assets can be edited
  @Bean(name = EveliAutoConfigAssets.BEAN_NAME)
  public EveliEditEnvir eveliEditEnvir(
      EveliProps eveliProps, 
      EveliPropsAssets assetProps,
      ObjectMapper objectMapper,
      ApplicationContext context,
      io.vertx.mutiny.sqlclient.Pool pgPool) {
    
    final var dev = EveliAutoConfigAssets.eveliEditEnvir(eveliProps, assetProps, objectMapper, context, pgPool);
    return dev;
  }
}
