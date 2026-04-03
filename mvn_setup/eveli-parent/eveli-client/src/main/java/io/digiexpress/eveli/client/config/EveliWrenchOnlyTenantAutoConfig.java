package io.digiexpress.eveli.client.config;

import java.util.Collections;
import java.util.List;

/*-
 * #%L
 * eveli-client
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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.digiexpress.eveli.client.api.ImmutableTenantConfig;
import io.digiexpress.eveli.client.api.TenantConfigClient;
import io.digiexpress.eveli.client.api.TenantConfigClient.TenantConfig;
import io.digiexpress.eveli.client.web.resources.assets.AssetsWrenchController;
import io.digiexpress.eveli.client.web.resources.worker.TenantApiController;
import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.program.Compiler;
import io.smallrye.mutiny.Uni;


@Configuration
public class EveliWrenchOnlyTenantAutoConfig {
  
  public static String FEATURE_WRENCH_ONLY = "wrench-only";
  public static String FEATURE_USER_PROFILE = "user_profile";

  @Bean 
  public AssetsWrenchController assetsWrenchController(Authoring composer, Compiler compiler) {
    return new AssetsWrenchController(composer, compiler) {
      @Override 
      public Uni<List<String>> flowNames() {
        return Uni.createFrom().item(Collections.emptyList()); 
      }
    };
  }
  
  @Bean
  public TenantApiController tenantApiController() {
    final Uni<TenantConfig> tenant = Uni.createFrom().item(ImmutableTenantConfig.builder().addFeatures(FEATURE_WRENCH_ONLY).build());
    
    return new TenantApiController(new TenantConfigClient() {
      @Override
      public TenantConfigClientConfigQuery createConfigQuery() {
        return new TenantConfigClientConfigQuery() { 
          @Override 
          public Uni<TenantConfig> getOne() { 
            return tenant; 
          } 
        };
      }
    });
  }
}
