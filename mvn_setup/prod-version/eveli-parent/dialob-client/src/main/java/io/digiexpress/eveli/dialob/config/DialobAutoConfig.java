package io.digiexpress.eveli.dialob.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer;

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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.dialob.spi.DialobClientImpl;
import io.digiexpress.eveli.dialob.spi.DialobService;



@Configuration
@EnableConfigurationProperties(value = {
    DialobConfigProps.class, 
})
public class DialobAutoConfig {
  
  
  @Bean 
  public DialobService dialobService(DialobConfigProps props) {
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
    
    final var forms = new RestTemplateBuilder()
        .uriTemplateHandler(new DefaultUriBuilderFactory(serviceUrl + "/dialob/api/forms"))
        .additionalInterceptors(interceptors)
        .build();

    final var questionnaires = new RestTemplateBuilder()
        .uriTemplateHandler(new DefaultUriBuilderFactory(serviceUrl + "/dialob/api/questionnaires"))
        .additionalInterceptors(interceptors)
        .build();

    final var sessions = new RestTemplateBuilder()
      .uriTemplateHandler(new DefaultUriBuilderFactory(serviceUrl + "/session/dialob"))
      .additionalInterceptors(interceptors)
      .build();
  
    return new DialobService(forms, sessions, questionnaires);
  }
  
  @Bean 
  public DialobClient dialobClient(DialobService service, ObjectMapper objectMapper) {
    return DialobClientImpl.builder()
        .objectMapper(objectMapper)
        .dialobService(service)
        .build();
  }  
}
