package io.digiexpress.eveli.app;

/*-
 * #%L
 * eveli-app
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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import io.digiexpress.eveli.client.config.EveliAutoConfig;
import io.digiexpress.eveli.client.config.EveliAutoConfigAssets;
import io.digiexpress.eveli.client.config.EveliAutoConfigDB;
import io.digiexpress.eveli.client.config.EveliAutoConfigGamut;
import io.digiexpress.eveli.dialob.config.DialobAutoConfig;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableAutoConfiguration
@EnableAsync
@Slf4j
@Import(value = { EveliAutoConfigDB.class, EveliAutoConfigAssets.class, EveliAutoConfig.class, DialobAutoConfig.class,
    EveliAutoConfigGamut.class })
public class Application {
  public static void main(String[] args) throws Exception {
    SpringApplication.run(new Class<?>[] { Application.class }, args);
  }

  @EventListener
  public void handleContextRefresh(ContextRefreshedEvent event) {
    final var applicationContext = event.getApplicationContext();
    final var requestMappingHandlerMapping = applicationContext.getBean("requestMappingHandlerMapping",
        RequestMappingHandlerMapping.class);

    final var newLog = new ApplicationConfigLogger().log(requestMappingHandlerMapping);
    log.info(newLog);
    
    
    
    
    /*
     *     final var endpoints = requestMappingHandlerMapping.getHandlerMethods();

    final var endpointCount = endpoints.size();

    final var msg = new StringBuilder("\r\nEVELI REST API\r\n");

    final var httpGETCount = endpoints.entrySet().stream().filter(entry -> entry.getKey().toString().contains("GET"))
        .count();
    final var httpPUTCount = endpoints.entrySet().stream().filter(entry -> entry.getKey().toString().contains("PUT"))
        .count();
    final var httpPOSTCount = endpoints.entrySet().stream().filter(entry -> entry.getKey().toString().contains("POST"))
        .count();
    final var httpDELETECount = endpoints.entrySet().stream()
        .filter(entry -> entry.getKey().toString().contains("DELETE")).count();

    Map<String, Set<String>> httpMethods = new HashMap<>();
    endpoints.entrySet().forEach(entry -> {

      String url = entry.getKey().toString();

      String[] parts = url.split(" ", 2);
      String httpMethod = parts[0];
      String endpointPath = parts[1];

      httpMethods.computeIfAbsent(endpointPath, k -> new HashSet<>()).add(httpMethod);
    });

    msg.append("Total endpoints: " + endpointCount).append("\r\n");
    msg.append("Total GET: ").append(httpGETCount).append("\r\n");
    msg.append("Total PUT: ").append(httpPUTCount).append("\r\n");
    msg.append("Total POST: ").append(httpPOSTCount).append("\r\n");
    msg.append("Total DELETE: ").append(httpDELETECount).append("\r\n");

    msg.append("\r\n------------------------------------------------------------------\r\n");
    msg.append("\r\n");
    msg.append("Grouped endpoints by base URL and associated HTTP methods:\r\n");

    Map<String, List<String>> groupedEndpoints = new HashMap<>();
    int baseUrlDepth = 4;

    for (Map.Entry<String, Set<String>> entry : httpMethods.entrySet()) {
      String endpoint = entry.getKey();
      Set<String> methods = entry.getValue();

      String cleanedEndpoint = endpoint
        .replace("[", "")
        .replace("]", "")
        .trim();
      
      if (cleanedEndpoint.contains("produces")) { 
        int producesIndex = cleanedEndpoint.indexOf("produces"); 
        cleanedEndpoint = cleanedEndpoint.substring(0, producesIndex).trim(); 
      }
      
      
      if (cleanedEndpoint.contains("consumes")) { 
        int consumesIndex = cleanedEndpoint.indexOf("consumes"); 
        cleanedEndpoint = cleanedEndpoint.substring(0, consumesIndex).trim(); 
      }
         
      String[] parts = cleanedEndpoint.split("/");

      StringBuilder baseUrl = new StringBuilder();
      for (int i = 0; i < Math.min(parts.length, baseUrlDepth); i++) {
        if (i > 0)
          baseUrl.append("/"); 
          baseUrl.append(parts[i]);
      }

      String baseUrlString = baseUrl.toString();
      groupedEndpoints.computeIfAbsent(baseUrlString, k -> new ArrayList<>())
        .add(cleanedEndpoint + " —> " + String.join(", ", methods.toString()
          .replace("{", "")
          .replace("[", "")
          .replace("]", "")
        ));
    }

    for (Map.Entry<String, List<String>> entry : groupedEndpoints.entrySet()) {
      String baseUrl = entry.getKey();
      List<String> endpointsInGroup = entry.getValue();

      msg.append("Group: ").append(baseUrl).append("\r\n");
      for (String endpoint : endpointsInGroup) {
        msg.append(endpoint).append("\r\n");
      }
      msg.append("\r\n");
    }

    endpoints.forEach((key, value) -> { 
      int length = value.toString().length();
      
      
      int start = value.toString().indexOf(".");
      int end = value.toString().indexOf("#");
      
      msg.append(value.toString().substring(start, end)).append("\r\n"); 
    });

    log.info(msg.toString());

    
     * 
     * String greenColor = "\033[32m"; String resetColor = "\033[0m";
     * 
     * msg.
     * append("\r\n--------------------------------- GET Endpoints ---------------------------------\r\n"
     * );
     * 
     * endpoints.forEach((key, value) -> { if (key.toString().contains("GET")) {
     * msg.append(greenColor); msg.append(key).append(" - ");
     * msg.append(resetColor); msg.append(value).append("\r\n"); } });
     * 
     * msg.
     * append("\r\n--------------------------------- PUT Endpoints ---------------------------------\r\n"
     * );
     * 
     * endpoints.forEach((key, value) -> { if (key.toString().contains("PUT") ) {
     * msg.append(key).append("  - ").append(" = ").append(value).append("\r\n"); }
     * });
     * 
     * msg.
     * append("\r\n--------------------------------- POST Endpoints ---------------------------------\r\n"
     * );
     * 
     * endpoints.forEach((key, value) -> { if (key.toString().contains("POST")) {
     * msg.append(key).append("  - ").append(" = ").append(value).append("\r\n"); }
     * });
     * 
     * msg.
     * append("\r\n--------------------------------- DELETE ---------------------------------\r\n"
     * );
     * 
     * endpoints.forEach((key, value) -> { if (key.toString().contains("DELETE")) {
     * msg.append(key).append("  - ").append(" = ").append(value).append("\r\n"); }
     * });
     */

  }
  // HHH015007 - https://hibernate.atlassian.net/browse/HHH-17612
}
