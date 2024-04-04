package io.resys.thena.structures.grim;

import java.time.OffsetDateTime;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ComparisonChain;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.spi.DbState;
import io.vertx.core.json.jackson.DatabindCodec;

public class GrimPrinter {
  private final DbState state;

  public GrimPrinter(DbState state) {
    super();
    this.state = state;
  } 
 
  public String print(Tenant repo) {
   return internalPrinting(repo, false, null);
  }
  public String printWithStaticIds(Tenant repo, Map<String, String> replacements) {
    return internalPrinting(repo, true, replacements);
  }
  
  
  public String internalPrinting(Tenant repo, boolean isStatic, final Map<String, String> collector) {
    final Map<String, String> replacements = collector != null ? collector : new HashMap<>();
    final Function<String, String> ID = (id) -> {
      if(!isStatic) {
        return id;
      }
      if(id == null) {
        return null;
      }
      
      if(replacements.containsKey(id)) {
        return replacements.get(id);
      }
      final var next = String.valueOf(replacements.size() + 1);
      replacements.put(id, next);
      return next;
    };
    
    final Function<OffsetDateTime, String> DATES = (input) -> {
      if(input == null) {
        return null;
      }
      try {
        final var id = DatabindCodec.mapper().writeValueAsString(input);
        if(!isStatic) {
          return id.toString();
        }
  
        if(replacements.containsKey(id)) {
          return replacements.get(id);
        }
        final var next = "\"OffsetDateTime.now()\"";
        replacements.put(id, next);
        return next;
      } catch(Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    };

    final var ctx = state.toGrimState(repo);
    
    StringBuilder result = new StringBuilder();

    result
    .append(System.lineSeparator())
    .append("Repo").append(System.lineSeparator())
    .append("  - id: ").append(ID.apply(repo.getId()))
    .append(", rev: ").append(ID.apply(repo.getRev())).append(System.lineSeparator())
    .append("    name: ").append(repo.getName())
    .append(", prefix: ").append(ID.apply(repo.getPrefix()))
    .append(", type: ").append(repo.getType()).append(System.lineSeparator());
    
    ctx.query().missions().findAll()
    .collect().asList()
    .onItem()
    .transform(items -> {
     
      for(final var data : items.stream().flatMap(e -> e.getCommits().values().stream())
          .sorted((a, b) -> ComparisonChain.start()
              .compare(a.getCreatedAt(), b.getCreatedAt())
              .result())
          .toList()
          ) {
        ID.apply(data.getParentCommitId());
        ID.apply(data.getCommitId());
        ID.apply(data.getMissionId());
        
        DATES.apply(data.getCreatedAt());
      }
      
      for(final var item : items.stream()
          .sorted((a, b) -> ComparisonChain.start()
              .compare(
                  ID.apply(a.getMissions().values().iterator().next().getId()), 
                  ID.apply(b.getMissions().values().iterator().next().getId())
              ).result())
          .toList()) {
        

        final var mission = item.getMissions().values().iterator().next();      
        result.append("Mission: ").append(ID.apply(mission.getId())).append(System.lineSeparator());


        
        for(final var data : item.getAssignments().values().stream()
            .sorted((a, b) -> ComparisonChain.start()
                .compare(ID.apply(a.getCommitId()), ID.apply(b.getCommitId()))
                .compare(a.getAssignmentType(), b.getAssignmentType())
                .compare(a.getAssignee(), b.getAssignee())
                .result())
            .toList()) {
          result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
        }
        for(final var data : item.getData().values().stream()
            .sorted((a, b) -> ComparisonChain.start()
                .compare(ID.apply(a.getCommitId()), ID.apply(b.getCommitId()))
                .compare(a.getTitle(), b.getTitle())
                .result())
            .toList()) {
          result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
        }
        
        for(final var data : item.getMissionLabels().values().stream()
            .sorted((a, b) -> ComparisonChain.start()
                .compare(ID.apply(a.getCommitId()), ID.apply(b.getCommitId()))
                .compare(a.getLabelType(), b.getLabelType())
                .compare(a.getLabelValue(), b.getLabelValue())
                .result())
            .toList()) {
          
          result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
        }
        for(final var data : item.getLinks().values().stream()
            .sorted((a, b) -> ComparisonChain.start()
                .compare(ID.apply(a.getCommitId()), ID.apply(b.getCommitId()))
                .compare(a.getLinkType(), b.getLinkType())
                .compare(a.getExternalId(), b.getExternalId())
                .result())
            .toList()) {
          
          result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
        }
        for(final var data : item.getObjectives().values().stream()
            .sorted((a, b) -> ComparisonChain.start()
                .compare(ID.apply(a.getCommitId()), ID.apply(b.getCommitId()))
                .compare(a.getObjectiveStatus()+ "", b.getObjectiveStatus()+ "")
                .compare(a.getStartDate()+ "", b.getStartDate()+ "")
                .compare(a.getDueDate()+ "", b.getDueDate()+ "")
                .result())
            .toList()) {
          
          result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
        }
        for(final var data : item.getGoals().values().stream()
            .sorted((a, b) -> ComparisonChain.start()
                .compare(ID.apply(a.getCommitId()), ID.apply(b.getCommitId()))
                .compare(a.getGoalStatus() + "", b.getGoalStatus()+ "")
                .compare(a.getStartDate()+ "", b.getStartDate()+ "")
                .compare(a.getDueDate()+ "", b.getDueDate()+ "")
                .result())
            .toList()) {
          result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
        }
        for(final var data : item.getRemarks().values().stream()          
            .sorted((a, b) -> ComparisonChain.start()
            .compare(ID.apply(a.getCommitId()), ID.apply(b.getCommitId()))
            .compare(a.getRemarkText(), b.getRemarkText())
            .compare(a.getReporterId()+ "", b.getReporterId()+ "")
            .compare(a.getRemarkStatus()+ "", b.getRemarkStatus()+ "")
            .result())
        .toList()) {
          
          result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
        }
        for(final var data : item.getCommands().values().stream()
            .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
            .toList()) {
          result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
        }
        
        
        for(final var data : item.getCommits().values().stream()
            .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
            .toList()) {
          
          var log = data.getCommitLog();
          
          for(final var entry : replacements.entrySet()) {
            log = log.replace(entry.getKey(), entry.getValue());
          }
          
          result
            .append(System.lineSeparator())
            .append(log)
            .append(System.lineSeparator());
        }      
        
        
      }
      
      return items;
    })
    .await().indefinitely();
    
    return result.toString();
  }
}
