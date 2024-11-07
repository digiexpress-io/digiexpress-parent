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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public class ApplicationConfigLogger {

  private final static String LN = "\r\n";
  private final List<LogEntry> entries = new ArrayList<>();
  private final Set<String> log_entry_paths = new HashSet<>();
  private final StringBuilder log_msg = new StringBuilder(LN);

  private final String greenColor = "\033[32m"; 
  private final String yellowColor = "\033[33m";
  private final String magentaColor = "\033[35m";
  private final String resetColor = "\033[0m";
  
  public String log(RequestMappingHandlerMapping mapping) {
    final var endpoints = mapping.getHandlerMethods();

    endpoints.forEach((key, value) -> createLogEntry(key, value));
    
    createSummary();
    
    
    
    createGroupedLogEntries();
    
    return log_msg.toString();
  }

  private void createSummary() {
    final var httpGETCount = entries.stream().filter(entry -> entry.getMethods().contains("GET")).count();
    final var httpPUTCount = entries.stream().filter(entry -> entry.getMethods().toString().contains("PUT")).count();
    final var httpPOSTCount = entries.stream().filter(entry -> entry.getMethods().toString().contains("POST")).count();
    final var httpDELETECount = entries.stream().filter(entry -> entry.getMethods().toString().contains("DELETE"))
        .count();

    log_msg.append("#-----------------------------------------------------------").append(LN).append("  Summary:").append(LN)
        .append("    Total-endpoints: ").append(entries.size()).append(LN)
        .append("    Total-GET: ").append(httpGETCount).append(LN)
        .append("    Total-PUT: ").append(httpPUTCount).append(LN)
        .append("    Total-POST: ").append(httpPOSTCount).append(LN)
        .append("    Total-DELETE: ")
        .append(httpDELETECount).append(LN).append("#-----------------------------------------------------------")
        .append(LN);

  }
  

  private void createLogEntry(RequestMappingInfo mappingInfo, HandlerMethod method) {
    final var pattern = mappingInfo.getActivePatternsCondition().toString();
    final var entry = LogEntry.builder().controller(method.getBeanType().getSimpleName())
        .path(pattern.substring(1, pattern.length() - 1))
        .methods(mappingInfo.getMethodsCondition().getMethods().stream()
            .map(m -> m.name())
            .toList())
        .build();
    entries.add(entry);
    
    log_entry_paths.add(entry.getPath());
        
  }
  
  
  private List<String> extractGroups() {
    List<String> groups = new ArrayList<>();
    for(final var control : entries) {
      
      final var controlSegments = control.getPath().split("\\/");
      
      for(final var test : entries) {
        if(test.getPath().equals(control.getPath())) {
          continue;
        }
        
        final var groupKey = getGroup(controlSegments, test.getPath());
        if(StringUtils.isEmpty(groupKey)) {
          continue;
        }
        if(!groups.contains(groupKey)) {
          groups.add(groupKey);
        }
      }  
    }
    return groups.stream()
        .filter(endpoint -> !(endpoint.equals("/rest") || endpoint.equals("/rest/api")))
        .filter(endpoint -> {
          return entries.stream().filter(e -> e.getPath().equals(endpoint)).count() == 0 ||
              endpoint.split("\\/").length == 2;
        })
        .sorted().toList();
  }
  
  private String getGroup(String[] control, String test) {
    final var longestCommon = new StringBuilder();
    for(final var common : control) {
      final var next = longestCommon.toString() + "/" + common;
      
      if(!test.startsWith(next) && longestCommon.length() > 1) {
        break;
      }
      if(!longestCommon.toString().endsWith("/")) {
        longestCommon.append("/");
      }
      longestCommon.append(common);
    }
    return longestCommon.toString();
  }
  


  private void createGroupedLogEntries() {
    
    for(final var groupPath : extractGroups()) {
      
      log_msg.append("  ").append(groupPath).append(":").append(LN);
      
      entries.stream().filter(e -> e.getPath().startsWith(groupPath))
        .forEach(entry -> {
          log_msg
            .append(greenColor)
            .append("    ")
            .append(entry.getPath())
            .append(resetColor)
            .append(" ")

            .append(yellowColor)
            .append(entry.getMethods().toString())
            .append(resetColor)
            
            .append(magentaColor)
            .append(": ").append(entry.getController())
            .append(resetColor)
            .append(LN);
        });
    }
  }


  @RequiredArgsConstructor
  @Data
  @Builder
  private static final class LogEntry {
    private final String path;
    private final List<String> methods;
    private final String controller;
  }

}
