package io.resys.thena.storesql.builders;


import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.datasource.SqlQueryBuilder;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.storesql.support.Execute;
import io.resys.thena.structures.doc.DocInserts;
import io.resys.thena.structures.doc.ImmutableDocBatchForMany;
import io.resys.thena.structures.doc.ImmutableDocBatchForOne;
import io.resys.thena.structures.git.GitInserts.BatchStatus;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DocDbInsertsSqlPool implements DocInserts {
  private final ThenaSqlDataSource wrapper;
  private final SqlQueryBuilder sqlBuilder;

  public DocDbInsertsSqlPool(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.sqlBuilder = dataSource.getQueryBuilder();
  }
  
  @Override
  public Uni<DocBatchForMany> batchMany(DocBatchForMany many) {
    final List<DocBatchForOne> output = many.getItems();
    RepoAssert.isTrue(this.wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    final var tx = wrapper.getClient();
    
    
    final var docInserts = sqlBuilder.docs().insertMany(output.stream()
        .filter(item -> item.getDocLock().isEmpty())
        .filter(item -> !item.getDoc().isEmpty())
        .map(item -> item.getDoc().get()).collect(Collectors.toList()));
    final var docUpdated = sqlBuilder.docs().updateMany(output.stream()
        .filter(item -> !item.getDocLock().isEmpty())
        .filter(item -> !item.getDoc().isEmpty())
        .map(item -> item.getDoc().get()).collect(Collectors.toList()));
    
    final var lockedBranchIds = output.stream()
        .flatMap(item -> item.getDocLock().stream())
        .map(branch -> branch.getBranch().get().getId())
        .collect(Collectors.toList());
    
    final var commitsInsert = sqlBuilder.docCommits()
        .insertAll(output.stream().flatMap(e -> e.getDocCommit().stream())
        .collect(Collectors.toList()));
    
    final var branchInsert = sqlBuilder.docBranches()
        .insertAll(output.stream().flatMap(e -> e.getDocBranch().stream())
        .filter(branch -> !lockedBranchIds.contains(branch.getId()))
        .collect(Collectors.toList()));
    
    final var branchUpdate = sqlBuilder.docBranches()
        .updateAll(output.stream().flatMap(e -> e.getDocBranch().stream())
        .filter(branch -> lockedBranchIds.contains(branch.getId()))
        .collect(Collectors.toList()));
    
    final var logsInsert = sqlBuilder.docLogs()
        .insertAll(output.stream().flatMap(e -> e.getDocLogs().stream())
        .collect(Collectors.toList()));
    
    
    final Uni<DocBatchForMany> docsUni1 = Execute.apply(tx, docInserts).onItem()
        .transform(row -> successOutput(many, "Doc inserted, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(many, "Failed to create docs", e));

    final Uni<DocBatchForMany> docsUni2 = Execute.apply(tx, docUpdated).onItem()
        .transform(row -> successOutput(many, "Doc updated, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(many, "Failed to update docs", e));
        
    final Uni<DocBatchForMany> commitUni = Execute.apply(tx, commitsInsert).onItem()
        .transform(row -> successOutput(many, "Commit saved, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(many, "Failed to save commit", e));

    final Uni<DocBatchForMany> branchUniInsert = Execute.apply(tx, branchInsert).onItem()
        .transform(row -> successOutput(many, "Branch saved, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(many, "Failed to save branch", e));
    
    final Uni<DocBatchForMany> branchUniUpdate = Execute.apply(tx, branchUpdate).onItem()
        .transform(row -> successOutput(many, "Branch saved, number of updates entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(many, "Failed to save branch", e));
    
    
    final Uni<DocBatchForMany> logUni = Execute.apply(tx, logsInsert).onItem()
        .transform(row -> successOutput(many, "Commit log saved, number of new entries: "  + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(many, "Failed to save  commit log", e));
    
    return Uni.combine().all().unis(docsUni1, docsUni2, commitUni, branchUniInsert, branchUniUpdate, logUni).asTuple()
        .onItem().transform(tuple -> merge(many,
                tuple.getItem1(), 
                tuple.getItem2(), 
                tuple.getItem3(), 
                tuple.getItem4(),
                tuple.getItem5(),
                tuple.getItem6()
        ));
  }

  
  @Override
  public Uni<DocBatchForOne> batchOne(DocBatchForOne output) {
    RepoAssert.isTrue(this.wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    final var tx = wrapper.getClient();
    
    final var docsInsert = output.getDoc().map(doc -> {
      if(output.getDocLock().isEmpty()) {
        return sqlBuilder.docs().insertOne(doc);  
      }
      return sqlBuilder.docs().updateOne(doc);
    });
    
    final var lockedBranchIds = output.getDocLock().stream().map(branch -> branch.getBranch().get().getId()).collect(Collectors.toList());
    final var commitsInsert = sqlBuilder.docCommits().insertAll(output.getDocCommit());
    final var branchInsert = sqlBuilder.docBranches().insertAll(output.getDocBranch().stream().filter(branch -> !lockedBranchIds.contains(branch.getId())).collect(Collectors.toList()));
    final var branchUpdate = sqlBuilder.docBranches().updateAll(output.getDocBranch().stream().filter(branch -> lockedBranchIds.contains(branch.getId())).collect(Collectors.toList()));
    final var logsInsert = sqlBuilder.docLogs().insertAll(output.getDocLogs());
    
    
    final Uni<DocBatchForOne> docsUni;
    if(docsInsert.isEmpty()) {
      docsUni = Uni.createFrom().item(successOutput(output, "Doc has no data, skipping doc entry"));    
    } else {
      docsUni = Execute.apply(tx, docsInsert.get()).onItem()
          .transform(row -> successOutput(output, "Doc saved, number of new entries: " + row.rowCount()))
          .onFailure().recoverWithItem(e -> failOutput(output, "Failed to create docs", e));
    }
    
    final Uni<DocBatchForOne> commitUni = Execute.apply(tx, commitsInsert).onItem()
      .transform(row -> successOutput(output, "Commit saved, number of new entries: " + row.rowCount()))
      .onFailure().recoverWithItem(e -> failOutput(output, "Failed to save commit \r\n" + output.getDocCommit(), e));

    final Uni<DocBatchForOne> branchUniInsert = Execute.apply(tx, branchInsert).onItem()
        .transform(row -> successOutput(output, "Branch saved, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(output, "Failed to save branch", e));
    
    final Uni<DocBatchForOne> branchUniUpdate = Execute.apply(tx, branchUpdate).onItem()
        .transform(row -> successOutput(output, "Branch saved, number of updates entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(output, "Failed to save branch", e));
    
    final Uni<DocBatchForOne> logUni = Execute.apply(tx, logsInsert).onItem()
        .transform(row -> successOutput(output, "Commit log saved, number of new entries: "  + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(output, "Failed to save  commit log", e));
    
    return Uni.combine().all().unis(docsUni, commitUni, branchUniInsert, branchUniUpdate, logUni).asTuple()
        .onItem().transform(tuple -> merge(output, 
            tuple.getItem1(), 
            tuple.getItem2(), 
            tuple.getItem3(), 
            tuple.getItem4(),
            tuple.getItem5()
        ));
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
  
  
  private DocBatchForOne merge(DocBatchForOne start, DocBatchForOne ... current) {
    final var builder = ImmutableDocBatchForOne.builder().from(start);
    final var log = new StringBuilder(start.getLog().getText());
    var status = start.getStatus();
    for(DocBatchForOne value : current) {
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
  private DocBatchForMany failOutput(DocBatchForMany current, String msg, Throwable t) {
    log.error("Batch failed because of: " + msg, t);
    return ImmutableDocBatchForMany.builder()
        .from(current)
        .status(BatchStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(msg).build())
        .addMessages(ImmutableMessage.builder().text(t.getMessage()).build())
        .build(); 
  }
  
  private DocBatchForOne successOutput(DocBatchForOne current, String msg) {
    return ImmutableDocBatchForOne.builder()
      .from(current)
      .status(BatchStatus.OK)
      .addMessages(ImmutableMessage.builder().text(msg).build())
      .build();
  }
  
  private DocBatchForOne failOutput(DocBatchForOne current, String msg, Throwable t) {
    log.error("Batch failed because of: " + msg, t);
    return ImmutableDocBatchForOne.builder()
        .from(current)
        .status(BatchStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(msg).build())
        .addMessages(ImmutableMessage.builder().text(t.getMessage()).build())
        .build(); 
  }
}
