package io.resys.hdes.spring.app;

import java.util.Arrays;

/*-
 * #%L
 * spring-app
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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import io.resys.hdes.spring.composer.ComposerAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration
@Import(ComposerAutoConfiguration.class)
public class Application {
  public static void main(String[] args) throws Exception {
    SpringApplication.run(new Class<?>[]{Application.class}, args);
  }

  @Bean
  public CorsFilter corsFilter() {
      CorsConfiguration config = new CorsConfiguration();
      config.addAllowedOrigin("http://127.0.0.1:3000");
      config.addAllowedOrigin("http://localhost:3000");
      config.addAllowedHeader("*");
      config.setAllowedMethods(Arrays.asList("*"));

      CorsConfigurationSource source = (req) -> config; 
      return new CorsFilter(source);
  }
  /*
  @Bean
  public HdesStore fileStore(ObjectMapper om) {
    final var store = HdesStoreFileImpl.builder()
        .objectMapper(om)
        .repoName("test-assets")
        .db("/test-assets-folder")
        .build();
    
    store.repo().createIfNot().await().atMost(Duration.ofMillis(200));
    
    return store;
  }*/
}
