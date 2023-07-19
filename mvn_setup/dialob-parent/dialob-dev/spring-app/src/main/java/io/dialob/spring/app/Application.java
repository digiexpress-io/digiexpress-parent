package io.dialob.spring.app;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@SpringBootApplication
public class Application {
  public static void main(String[] args) throws Exception {
    try {
      SpringApplication.run(new Class<?>[] { Application.class }, args);
      //browse("http://localhost:8081/ide");
    } catch (Exception e) {
      e.printStackTrace();
    }
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

  public static void browse(String url) {
    if (Desktop.isDesktopSupported()) {
      Desktop desktop = Desktop.getDesktop();
      try {
        desktop.browse(new URI(url));
        return;
      } catch (IOException | URISyntaxException e) {
        e.printStackTrace();
      }
      return;
    }

    Runtime runtime = Runtime.getRuntime();
    try {
      runtime.exec("xdg-open " + url);
      return;
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
