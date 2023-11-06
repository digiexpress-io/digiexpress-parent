package io.resys.thena.docdb.spi;

import io.resys.thena.docdb.api.models.Repo;

public class DocDbPrinter {
  private final DbState state;

  public DocDbPrinter(DbState state) {
    super();
    this.state = state;
  }
  
  public String print(Repo repo) {
   final var ctx = state.toDocState().withRepo(repo);
    
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
      result.append("  - id: ").append(item.getId())
      .append(System.lineSeparator())
      .append("    doc id: ").append(item.getDocId())
      .append(", branch id: ").append(item.getBranchId())
      .append(", dateTime: ").append(item.getDateTime())
      .append(", parent: ").append(item.getParent().orElse(""))
      .append(", message: ").append(item.getMessage())
      .append(", author: ").append(item.getAuthor())
      .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    result
    .append(System.lineSeparator())
    .append("Logs").append(System.lineSeparator());
    
    ctx.query().logs()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(item.getId()).append(System.lineSeparator())
      .append("    doc id: ").append(item.getDocCommitId())
      .append(System.lineSeparator())
      .append("    log value: ").append(item.getValue())
      .append(System.lineSeparator())
      ;
      
      return item;
    }).collect().asList().await().indefinitely();
    
    return result.toString();
  }
}
