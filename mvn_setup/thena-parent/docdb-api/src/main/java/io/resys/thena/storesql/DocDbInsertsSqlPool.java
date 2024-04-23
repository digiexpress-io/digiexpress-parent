package io.resys.thena.storesql;


import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.api.registry.DocRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.storesql.support.Execute;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.doc.DocInserts;
import io.resys.thena.structures.doc.ImmutableDocBatchForMany;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DocDbInsertsSqlPool implements DocInserts {
  private final ThenaSqlDataSource wrapper;
  private final DocRegistry registry;

  public DocDbInsertsSqlPool(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.registry = dataSource.getRegistry().doc();
  }
  
  @Override
  public Uni<DocBatchForMany> batchMany(DocBatchForMany many) {
    final List<DocBatchForOne> output = many.getItems();
    RepoAssert.isTrue(this.wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    final var tx = wrapper.getClient();
    
    
    final var docInserts = registry.docs().insertMany(output.stream()
        .filter(item -> item.getDocLock().isEmpty())
        .filter(item -> !item.getDoc().isEmpty())
        .map(item -> item.getDoc().get()).collect(Collectors.toList()));
    final var docUpdated = registry.docs().updateMany(output.stream()
        .filter(item -> !item.getDocLock().isEmpty())
        .filter(item -> !item.getDoc().isEmpty())
        .map(item -> item.getDoc().get()).collect(Collectors.toList()));
    
    final var lockedBranchIds = output.stream()
        .flatMap(item -> item.getDocLock().stream())
        .map(branch -> branch.getBranch().get().getId())
        .collect(Collectors.toList());
    
    final var commitsInsert = registry.docCommits()
        .insertAll(output.stream().flatMap(e -> e.getDocCommit().stream())
        .collect(Collectors.toList()));
    
    final var branchInsert = registry.docBranches()
        .insertAll(output.stream().flatMap(e -> e.getDocBranch().stream())
        .filter(branch -> !lockedBranchIds.contains(branch.getId()))
        .collect(Collectors.toList()));
    
    final var branchUpdate = registry.docBranches()
        .updateAll(output.stream().flatMap(e -> e.getDocBranch().stream())
        .filter(branch -> lockedBranchIds.contains(branch.getId()))
        .collect(Collectors.toList()));
    
    final var logsInsert = registry.docCommitTrees()
        .insertAll(output.stream().flatMap(e -> e.getDocCommitTree().stream())
        .collect(Collectors.toList()));
    
    final var commandsInsert = registry.docCommands()
        .insertAll(output.stream().flatMap(e -> e.getDocCommands().stream())
        .collect(Collectors.toList()));
    
    
    
    final Uni<DocBatchForMany> docsUni1 = Execute.apply(tx, docInserts).onItem()
        .transform(row -> successOutput(many, "Doc inserted, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(many, "Failed to create docs", e));

    final Uni<DocBatchForMany> docsUni2 = Execute.apply(tx, docUpdated).onItem()
        .transform(row -> successOutput(many, "Doc updated, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(many, "Failed to update docs", e));
        
    final Uni<DocBatchForMany> commitUni = Execute.apply(tx, commitsInsert).onItem()
        .transform(row -> successOutput(many, "Commit saved, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(many, "Failed to save commit", e));

    final Uni<DocBatchForMany> branchUniInsert = Execute.apply(tx, branchInsert).onItem()
        .transform(row -> successOutput(many, "Branch saved, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(many, "Failed to save branch", e));
    
    final Uni<DocBatchForMany> branchUniUpdate = Execute.apply(tx, branchUpdate).onItem()
        .transform(row -> successOutput(many, "Branch saved, number of updates entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(many, "Failed to save branch", e));
    
    final Uni<DocBatchForMany> logUni = Execute.apply(tx, logsInsert).onItem()
        .transform(row -> successOutput(many, "Commit trees saved, number of new entries: "  + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(many, "Failed to save commit trees", e));
    
    final Uni<DocBatchForMany> commandsUni = Execute.apply(tx, commandsInsert).onItem()
        .transform(row -> successOutput(many, "Commands saved, number of new entries: "  + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(many, "Failed to save commands", e));
    
    
    return Uni.combine().all().unis(docsUni1, docsUni2, commitUni, branchUniInsert, branchUniUpdate, logUni, commandsUni).asTuple()
        .onItem().transform(tuple -> merge(many,
                tuple.getItem1(), 
                tuple.getItem2(), 
                tuple.getItem3(), 
                tuple.getItem4(),
                tuple.getItem5(),
                tuple.getItem6(),
                tuple.getItem7()
        ))
        .onFailure(DocBatchManyException.class)
        .recoverWithUni((ex) -> {
          final var batchError = (DocBatchManyException) ex;
          return tx.rollback().onItem().transform(junk -> batchError.getBatch());
        });
  }

  

  private DocBatchForMany merge(DocBatchForMany start, DocBatchForMany ... current) {
    final var builder = ImmutableDocBatchForMany.builder().from(start);
    final var log = new StringBuilder(start.getLog().getText());
    var status = start.getStatus();
    for(DocBatchForMany value : current) {
      if(value == null) {
        continue;
      }
      
      if(status != BatchStatus.ERROR) {
        status = value.getStatus();
      }
      log.append("\r\n\r\n").append(value.getLog());
      builder.addAllMessages(value.getMessages());
    }
    
    return builder.status(status).build();
  }  

  private DocBatchForMany successOutput(DocBatchForMany current, String msg) {
    return ImmutableDocBatchForMany.builder()
      .from(current)
      .status(BatchStatus.OK)
      .addMessages(ImmutableMessage.builder().text(msg).build())
      .build();
  }
  private DocBatchManyException failOutput(DocBatchForMany current, String msg, Throwable t) {
    log.error("Batch failed because of: " + msg, t);
    return new DocBatchManyException(ImmutableDocBatchForMany.builder()
        .from(current)
        .status(BatchStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(msg).exception(t).build())
        .build()); 
  }

  public static class DocBatchOneException extends RuntimeException {
    private static final long serialVersionUID = -7251738425609399151L;
    private final DocBatchForOne batch;
    
    public DocBatchOneException(DocBatchForOne batch) {
      this.batch = batch;
    }
    public DocBatchForOne getBatch() {
      return batch;
    }
  } 
  
  public static class DocBatchManyException extends RuntimeException {
    private static final long serialVersionUID = -7251738425609399151L;
    private final DocBatchForMany batch;
    
    public DocBatchManyException(DocBatchForMany batch) {
      this.batch = batch;
    }
    public DocBatchForMany getBatch() {
      return batch;
    }
  } 
}
