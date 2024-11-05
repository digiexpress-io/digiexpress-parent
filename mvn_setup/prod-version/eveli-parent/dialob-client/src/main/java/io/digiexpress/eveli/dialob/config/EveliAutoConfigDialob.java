package io.digiexpress.eveli.dialob.config;

/*-
 * #%L
 * dialob-client
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

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.dialob.spi.DialobClientImpl;



@Configuration
@EnableConfigurationProperties( value = {
    EveliPropsDialob.class, 
})
public class EveliAutoConfigDialob {


  @Bean 
  public DialobClient dialobClient(EveliPropsDialob config, ObjectMapper objectMapper) {
    final var restTemplate = new RestTemplate();
    final var dialob = DialobClientImpl.builder()
        .objectMapper(objectMapper)
        .restTemplate(restTemplate)
        .authorization(config.getApiKey())
        .serviceUrl(config.getServiceUrl())
        
        .url(config.getServiceUrl() + "/dialob/api/questionnaires")
        .formUrl(config.getServiceUrl() + "/dialob/api/forms")
        .sessionUrl(config.getServiceUrl() + "/session/dialob")
        .submitCallbackUrl(config.getServiceUrl() + "/dialobSubmitCallback")
        .build();
    return dialob;
  }  
}
