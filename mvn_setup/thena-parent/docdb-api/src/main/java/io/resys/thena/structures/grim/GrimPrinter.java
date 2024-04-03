package io.resys.thena.structures.grim;

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

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.spi.DbState;

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
      if(replacements.containsKey(id)) {
        return replacements.get(id);
      }
      final var next = String.valueOf(replacements.size() + 1);
      replacements.put(id, next);
      return next;
    };

    final var ctx = state.toGrimState(repo);
    
    StringBuilder result = new StringBuilder();

    result
    .append(System.lineSeparator())
    .append("Repo").append(System.lineSeparator())
    .append("  - id: ").append(ID.apply(repo.getId()))
    .append(", rev: ").append(ID.apply(repo.getRev())).append(System.lineSeparator())
    .append("    name: ").append(repo.getName())
    .append(", prefix: ").append(repo.getPrefix())
    .append(", type: ").append(repo.getType()).append(System.lineSeparator());
    
    ctx.query().missions().findAll()
    .onItem()
    .transform(item -> {
      final var mission = item.getMissions().values().iterator().next();      
      result.append("Mission: ").append(ID.apply(mission.getId())).append(System.lineSeparator());
      
      for(final var data : item.getData().values()) {
        result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
      }
      for(final var data : item.getMissionLabels().values()) {
        result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
      }
      for(final var data : item.getLinks().values()) {
        result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
      }
      for(final var data : item.getObjectives().values()) {
        result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
      }
      for(final var data : item.getGoals().values()) {
        result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
      }
      for(final var data : item.getRemarks().values()) {
        result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
      }
      for(final var data : item.getCommands().values()) {
        result.append("  - ").append(ID.apply(data.getId())).append("::").append(data.getDocType()).append(System.lineSeparator());
      }
      for(final var data : item.getCommits().values()) {
        result
          .append(System.lineSeparator())
          .append(data.getCommitLog())
          .append(System.lineSeparator());
      }      
      return item;
      
    })
    .collect().asList().await().indefinitely();;
    
    return result.toString();
  }
}
