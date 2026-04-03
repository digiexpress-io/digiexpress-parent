package io.digiexpress.eveli.client.config;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

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
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.limaone.spi.dialob.FormDb;
import io.resys.limaone.spi.dialob.FormDbImpl;



@Configuration
@EnableConfigurationProperties(value = {
  DialobConfigProps.class, 
})
public class DialobAutoConfig {
  
  @Bean 
  public FormDb dialobService(RestTemplateBuilder restTemplateBuilder, DialobConfigProps props, ObjectMapper objectMapper) {
    final var timeout = props.getConnectionTimeout() == null ? 100000 : props.getConnectionTimeout(); 
    
    final var serviceUrl = props.getServiceUrl();
    
    final ClientHttpRequestInterceptor interceptor = (HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
      final var headers = request.getHeaders();
      if(!ObjectUtils.isEmpty(props.getApiKey())) {
        headers.set("x-api-key", props.getApiKey());
      }
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      headers.setContentType(MediaType.APPLICATION_JSON);
      return execution.execute(request, body);    
    };
    
    final var interceptors = Collections.singletonList(interceptor);
    
    final var api = restTemplateBuilder
        .uriTemplateHandler(new DefaultUriBuilderFactory(props.getApiUrl().orElse(serviceUrl + "/dialob/api")))
        .additionalInterceptors(interceptors)
        .connectTimeout(Duration.ofMillis(timeout))
        .build();

    final var sessions = restTemplateBuilder
      .uriTemplateHandler(new DefaultUriBuilderFactory(props.getSessionUrl().orElse(serviceUrl + "/session/dialob")))
      .additionalInterceptors(interceptors)
      .connectTimeout(Duration.ofMillis(timeout))
      .build();
  
    return FormDbImpl.builder()
          .objectMapper(objectMapper)
          .formHttp(api)
          .questionnaireHttp(sessions)
          .build();
  }
  
}
