package io.digiexpress.eveli.app;

/*-
 * #%L
 * eveli-app
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

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.web.resources.assets.AssetsDeploymentController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsPublicationControllerReadOnly;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.ExternalDeploymentProvider;


@Profile("prod")
@Configuration
public class AppConfigProd {
  
  @Value("classpath:test-assets.json")
  Resource prodDeployment;
  
  @Bean
  public ExternalDeploymentProvider classpathExternalDeploymentProvider(ObjectMapper om) throws IOException {
    return new ClasspathExternalDeploymentProvider(prodDeployment.getInputStream(), om);
  }
  
  @Bean
  public AssetsPublicationControllerReadOnly assetsPublicationControllerReadOnly(EveliEnvirClient envirClient) {
    return new AssetsPublicationControllerReadOnly(envirClient);
  }
  
  @Bean
  public AssetsDeploymentController AssetsDeploymentController(WorkerAuthClient authClient, EveliEnvirClient envirClient, ApplicationEventPublisher publisher) {
    return new AssetsDeploymentController(authClient, envirClient, publisher);
  }
  
}
