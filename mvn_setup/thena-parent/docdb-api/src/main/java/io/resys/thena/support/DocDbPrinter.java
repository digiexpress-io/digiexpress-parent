package io.resys.thena.support;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.spi.DbState;

public class DocDbPrinter {
  private final DbState state;

  public DocDbPrinter(DbState state) {
    super();
    this.state = state;
  }
  
  private String removeAttr(String target, String attr) {
    
    var proc = target;
    var index = -1;
    while((index = proc.indexOf(attr + "\":")) > -1) {
      final var start = proc.substring(0, index -1);
      final var end = proc.substring(index);
      
      proc = start + end.substring(end.indexOf(",")+1);
    }
    
    return proc;
  }
  public String printWithStaticIds(Tenant repo) {
    final Map<String, String> replacements = new HashMap<>();
    return printWithStaticIds(repo, replacements);
  }
  public String printWithStaticIds(Tenant repo, Map<String, String> init) {
    return printWithStaticIds(repo, init, false);
  }
  public String printWithStaticIds(Tenant repo, Map<String, String> init, boolean extId) {
    final Map<String, String> replacements = new HashMap<>(init);
    final Function<String, String> ID = (id) -> {
      if(replacements.containsKey(id)) {
        return replacements.get(id);
      }
      final var next = String.valueOf(replacements.size() + 1);
      replacements.put(id, next);
      return next;
    };
  
    ID.apply(repo.getId());
    
    final var ctx = state.toDocState(repo);
    final StringBuilder result = new StringBuilder();
    
    result
    .append(System.lineSeparator())
    .append("Docs").append(System.lineSeparator());
    
    ctx.query().docs()
    .findAll().onItem()
    .transform(item -> {
      result
        .append("  - ")
        .append(ID.apply(item.getId())).append(": ").append(extId ? ID.apply(item.getExternalId()) : item.getExternalId())
        .append(System.lineSeparator());      
      return item;
    }).collect().asList().await().indefinitely();

    
    result
    .append(System.lineSeparator())
    .append("Branches").append(System.lineSeparator());
    
    ctx.query().branches()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - branch name: ").append(item.getBranchName())
      .append(System.lineSeparator())
      .append("    branch id: ").append(ID.apply(item.getId())).append(System.lineSeparator())
      .append("    doc id: ").append(item.getDocId()).append(System.lineSeparator())      
      .append("    commit id: ").append(ID.apply(item.getCommitId())).append(System.lineSeparator())
      
      .append("    ").append(item.getValue().toString())
      .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    result
    .append(System.lineSeparator())
    .append("Commands").append(System.lineSeparator());
    
    
    ctx.query().commands()
    .findAll().onItem()
    .transform(item -> {
      
      result.append("  - commands for doc: ").append(item.getDocId())
      .append(System.lineSeparator())
      .append("    commands id: ").append(ID.apply(item.getId())).append(System.lineSeparator())
      .append("    branch id: ").append(item.getBranchId().orElse("")).append(System.lineSeparator())
      .append("    value: ").append(item.getCommands()).append(System.lineSeparator())      

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
      .append("    doc id: ").append(ID.apply(item.getDocId()))
      .append(", branch id: ").append(ID.apply(item.getBranchId().orElse(null)))
      .append(", parent: ").append(ID.apply(item.getParent().orElse("")))
      .append(", message: ").append(item.getCommitMessage())
      .append(", author: ").append(item.getCommitAuthor())
      .append(System.lineSeparator())
      
      .append("  - commit log:").append(System.lineSeparator())
      .append(item.getCommitLog())
      .append(System.lineSeparator())
      .append(System.lineSeparator());
      
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    result
    .append(System.lineSeparator())
    .append("Logs").append(System.lineSeparator());
    
    ctx.query().trees()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ")
      .append(ID.apply(item.getId())).append("::").append(item.getBodyType()).append(System.lineSeparator())
      .append("    log patch: ").append(item.getBodyPatch())
      .append("    log before: ").append(item.getBodyBefore())
      .append("    log after: ").append(item.getBodyAfter())
      .append(System.lineSeparator())
      ;
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    var log = result.toString();
    for(final var entry : replacements.entrySet()) {
      if(entry.getKey() == null || entry.getKey().isEmpty()) {
        continue;
      }
      log = log.replace(entry.getKey(), entry.getValue());
    } 
    
    log = removeAttr(log, "createdAt");
    log = removeAttr(log, "updatedAt");
    
    return log;
  }
  
  public String print(Tenant repo) {
   final var ctx = state.toDocState(repo);
    
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
    .append("Docs").append(System.lineSeparator());
    
    ctx.query().docs()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ")
      .append(item.getId()).append(": ").append(item.getExternalId())
      .append(System.lineSeparator());
      return item;
    }).collect().asList().await().indefinitely();

    
    result
    .append(System.lineSeparator())
    .append("Branches").append(System.lineSeparator());
    
    ctx.query().branches()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - branch name: ").append(item.getBranchName()).append("/").append(item.getId())
      .append(System.lineSeparator())
      .append("    doc id: ").append(item.getDocId())
      .append(System.lineSeparator())
      .append("    commit id: ").append(item.getCommitId())
      .append(System.lineSeparator())
      
      .append("    ").append(item.getValue().toString())
      .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    result
    .append(System.lineSeparator())
    .append("Commits").append(System.lineSeparator());
    
    ctx.query().commits()
    .findAll().onItem()
    .transform(item -> {
      result
      .append("  - id: ").append(item.getId()).append(System.lineSeparator())
      .append("    doc id: ").append(item.getDocId())
      .append(", branch id: ").append(item.getBranchId())
      .append(", dateTime: ").append(item.getCreatedAt())
      .append(", parent: ").append(item.getParent().orElse(""))
      .append(", message: ").append(item.getCommitMessage())
      .append(", author: ").append(item.getCommitAuthor())
      .append(System.lineSeparator())
      
      .append("  - commit log:").append(System.lineSeparator())
      .append(item.getCommitLog())
      .append(System.lineSeparator())
      .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    result
    .append(System.lineSeparator())
    .append("Logs").append(System.lineSeparator());
    
    ctx.query().trees()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(item.getId()).append("::").append(item.getBodyType()).append(System.lineSeparator())
      .append("    log patch: ").append(item.getBodyPatch())
      .append("    log before: ").append(item.getBodyBefore())
      .append("    log after: ").append(item.getBodyAfter())
      .append(System.lineSeparator())
      ;
      
      return item;
    }).collect().asList().await().indefinitely();
    
    return result.toString();
  }
}
