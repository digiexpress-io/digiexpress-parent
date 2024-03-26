package io.resys.thena.structures.git;

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

import io.resys.thena.api.models.Diff;
import io.resys.thena.api.models.Repo;
import io.resys.thena.spi.DbState;

public class GitPrinter {
  private final DbState state;

  public GitPrinter(DbState state) {
    super();
    this.state = state;
  } 
  
  public String print(Diff diff) {
    final var repo = diff.getRepo();
    
    StringBuilder result = new StringBuilder();
    
    result.append(System.lineSeparator())
      .append("Diff").append(System.lineSeparator())
      .append("  - id: ").append(repo.getId())
      .append(", rev: ").append(repo.getRev())
      .append(System.lineSeparator()).append(System.lineSeparator());
    
    result.append("Divergences").append(System.lineSeparator());
    for(var divergence : diff.getDivergences()) {
      result
        .append("  - head: ").append(divergence.getHead().getCommit().getId()).append(" alias: ").append(String.join(", ", divergence.getHead().getRefs())).append(System.lineSeparator())
        .append("    main: ").append(divergence.getMain().getCommit().getId()).append(" alias: ").append(String.join(", ", divergence.getMain().getRefs())).append(System.lineSeparator())
        .append("    commits in main: ").append(divergence.getMain().getCommits()).append(System.lineSeparator())
        .append("    commits in head: ").append(divergence.getHead().getCommits()).append(System.lineSeparator());
    }
    return result.toString();
  }
  
  public String print(Repo repo) {
   final var ctx = state.toGitState().withRepo(repo);
    
    StringBuilder result = new StringBuilder();

    result
    .append(System.lineSeparator())
    .append("Repo").append(System.lineSeparator())
    .append("  - id: ").append(repo.getId())
    .append(", rev: ").append(repo.getRev()).append(System.lineSeparator())
    .append("    name: ").append(repo.getName())
    .append(", prefix: ").append(repo.getPrefix())
    .append(", type: ").append(repo.getType()).append(System.lineSeparator());
    
    result
    .append(System.lineSeparator())
    .append("Refs").append(System.lineSeparator());
    
    ctx.query().refs()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ")
      .append(item.getCommit()).append(": ").append(item.getName())
      .append(System.lineSeparator());
      return item;
    }).collect().asList().await().indefinitely();

    
    result
    .append(System.lineSeparator())
    .append("Tags").append(System.lineSeparator());
    
    ctx.query().tags()
    .find().onItem()
    .transform(item -> {
      result.append("  - id: ").append(item.getName())
      .append(System.lineSeparator())
      .append("    commit: ").append(item.getCommit())
      .append(", dateTime: ").append(item.getDateTime())
      .append(", message: ").append(item.getMessage())
      .append(", author: ").append(item.getAuthor())
      .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    result
    .append(System.lineSeparator())
    .append("Commits").append(System.lineSeparator());
    
    ctx.query().commits()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(item.getId())
      .append(System.lineSeparator())
      .append("    tree: ").append(item.getTree())
      .append(", dateTime: ").append(item.getDateTime())
      .append(", parent: ").append(item.getParent().orElse(""))
      .append(", message: ").append(item.getMessage())
      .append(", author: ").append(item.getAuthor())
      .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    result
    .append(System.lineSeparator())
    .append("Trees").append(System.lineSeparator());
    
    ctx.query().trees()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(item.getId()).append(System.lineSeparator());
      item.getValues().entrySet().forEach(e -> {
        result.append("    ")
          .append(e.getValue().getBlob())
          .append(": ")
          .append(e.getValue().getName())
          .append(System.lineSeparator());
      });
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    
    result
    .append(System.lineSeparator())
    .append("Blobs").append(System.lineSeparator());
    
    ctx.query().blobs()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ").append(item.getId()).append(": ").append(item.getValue()).append(System.lineSeparator());
      return item;
    }).collect().asList().await().indefinitely();
    
    return result.toString();
  }

  public String printWithStaticIds(Repo repo) {
    final Map<String, String> replacements = new HashMap<>();
    final Function<String, String> ID = (id) -> {
      if(replacements.containsKey(id)) {
        return replacements.get(id);
      }
      final var next = String.valueOf(replacements.size() + 1);
      replacements.put(id, next);
      return next;
    };

    final var ctx = state.toGitState().withRepo(repo);
    
    StringBuilder result = new StringBuilder();

    result
    .append(System.lineSeparator())
    .append("Repo").append(System.lineSeparator())
    .append("  - id: ").append(ID.apply(repo.getId()))
    .append(", rev: ").append(ID.apply(repo.getRev())).append(System.lineSeparator())
    .append("    name: ").append(repo.getName())
    .append(", prefix: ").append(repo.getPrefix())
    .append(", type: ").append(repo.getType()).append(System.lineSeparator());
    
    result
    .append(System.lineSeparator())
    .append("Refs").append(System.lineSeparator());
    
    ctx.query().refs()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ")
      .append(ID.apply(item.getCommit())).append(": ").append(item.getName())
      .append(System.lineSeparator());
      return item;
    }).collect().asList().await().indefinitely();

    
    result
    .append(System.lineSeparator())
    .append("Tags").append(System.lineSeparator());
    
    ctx.query().tags()
    .find().onItem()
    .transform(item -> {
      result.append("  - id: ").append(item.getName())
      .append(System.lineSeparator())
      .append("    commit: ").append(ID.apply(item.getCommit()))
      .append(", message: ").append(item.getMessage())
      .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    result
    .append(System.lineSeparator())
    .append("Commits").append(System.lineSeparator());
    
    ctx.query().commits()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(ID.apply(item.getId()))
      .append(System.lineSeparator())
      .append("    tree: ").append(ID.apply(item.getTree()))
      .append(", parent: ").append(item.getParent().map(e -> ID.apply(e)).orElse(""))
      .append(", message: ").append(item.getMessage())
      .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    result
    .append(System.lineSeparator())
    .append("Trees").append(System.lineSeparator());
    
    ctx.query().trees()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(ID.apply(item.getId())).append(System.lineSeparator());
      item.getValues().entrySet().forEach(e -> {
        result.append("    ")
          .append(e.getValue().getBlob())
          .append(": ")
          .append(e.getValue().getName())
          .append(System.lineSeparator());
      });
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    
    result
    .append(System.lineSeparator())
    .append("Blobs").append(System.lineSeparator());
    
    ctx.query().blobs()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ").append(item.getId()).append(": ").append(item.getValue()).append(System.lineSeparator());
      return item;
    }).collect().asList().await().indefinitely();
    
    return result.toString();
  }
}
