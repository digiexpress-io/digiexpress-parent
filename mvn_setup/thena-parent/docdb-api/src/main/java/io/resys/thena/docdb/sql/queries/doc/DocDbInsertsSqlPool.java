package io.resys.thena.docdb.sql.queries.doc;


import java.util.List;
import java.util.stream.Collectors;

/*-
 * #%L
 * thena-docdb-pgsql
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

import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.spi.DocDbInserts;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.spi.GitDbInserts.BatchStatus;
import io.resys.thena.docdb.spi.ImmutableDocDbBatchForMany;
import io.resys.thena.docdb.spi.ImmutableDocDbBatchForOne;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.docdb.sql.SqlBuilder;
import io.resys.thena.docdb.sql.SqlMapper;
import io.resys.thena.docdb.sql.support.Execute;
import io.resys.thena.docdb.sql.support.SqlClientWrapper;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DocDbInsertsSqlPool implements DocDbInserts {
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  @Override
  public Uni<DocDbBatchForMany> batchMany(DocDbBatchForMany many) {
    final List<DocDbBatchForOne> output = many.getItems();
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
    
    
    final Uni<DocDbBatchForMany> docsUni1 = Execute.apply(tx, docInserts).onItem()
        .transform(row -> successOutput(many, "Doc inserted, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(many, "Failed to create docs", e));

    final Uni<DocDbBatchForMany> docsUni2 = Execute.apply(tx, docUpdated).onItem()
        .transform(row -> successOutput(many, "Doc updated, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(many, "Failed to update docs", e));
        
    final Uni<DocDbBatchForMany> commitUni = Execute.apply(tx, commitsInsert).onItem()
        .transform(row -> successOutput(many, "Commit saved, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(many, "Failed to save commit", e));

    final Uni<DocDbBatchForMany> branchUniInsert = Execute.apply(tx, branchInsert).onItem()
        .transform(row -> successOutput(many, "Branch saved, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(many, "Failed to save branch", e));
    
    final Uni<DocDbBatchForMany> branchUniUpdate = Execute.apply(tx, branchUpdate).onItem()
        .transform(row -> successOutput(many, "Branch saved, number of updates entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(many, "Failed to save branch", e));
    
    
    final Uni<DocDbBatchForMany> logUni = Execute.apply(tx, logsInsert).onItem()
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
  public Uni<DocDbBatchForOne> batchOne(DocDbBatchForOne output) {
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
    
    
    final Uni<DocDbBatchForOne> docsUni;
    if(docsInsert.isEmpty()) {
      docsUni = Uni.createFrom().item(successOutput(output, "Doc has no data, skipping doc entry"));    
    } else {
      docsUni = Execute.apply(tx, docsInsert.get()).onItem()
          .transform(row -> successOutput(output, "Doc saved, number of new entries: " + row.rowCount()))
          .onFailure().recoverWithItem(e -> failOutput(output, "Failed to create docs", e));
    }
    
    final Uni<DocDbBatchForOne> commitUni = Execute.apply(tx, commitsInsert).onItem()
      .transform(row -> successOutput(output, "Commit saved, number of new entries: " + row.rowCount()))
      .onFailure().recoverWithItem(e -> failOutput(output, "Failed to save commit \r\n" + output.getDocCommit(), e));

    final Uni<DocDbBatchForOne> branchUniInsert = Execute.apply(tx, branchInsert).onItem()
        .transform(row -> successOutput(output, "Branch saved, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(output, "Failed to save branch", e));
    
    final Uni<DocDbBatchForOne> branchUniUpdate = Execute.apply(tx, branchUpdate).onItem()
        .transform(row -> successOutput(output, "Branch saved, number of updates entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(output, "Failed to save branch", e));
    
    final Uni<DocDbBatchForOne> logUni = Execute.apply(tx, logsInsert).onItem()
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

  private DocDbBatchForMany merge(DocDbBatchForMany start, DocDbBatchForMany ... current) {
    final var builder = ImmutableDocDbBatchForMany.builder().from(start);
    final var log = new StringBuilder(start.getLog().getText());
    var status = start.getStatus();
    for(DocDbBatchForMany value : current) {
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
  
  
  private DocDbBatchForOne merge(DocDbBatchForOne start, DocDbBatchForOne ... current) {
    final var builder = ImmutableDocDbBatchForOne.builder().from(start);
    final var log = new StringBuilder(start.getLog().getText());
    var status = start.getStatus();
    for(DocDbBatchForOne value : current) {
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
  private DocDbBatchForMany successOutput(DocDbBatchForMany current, String msg) {
    return ImmutableDocDbBatchForMany.builder()
      .from(current)
      .status(BatchStatus.OK)
      .addMessages(ImmutableMessage.builder().text(msg).build())
      .build();
  }
  private DocDbBatchForMany failOutput(DocDbBatchForMany current, String msg, Throwable t) {
    log.error("Batch failed because of: " + msg, t);
    return ImmutableDocDbBatchForMany.builder()
        .from(current)
        .status(BatchStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(msg).build())
        .build(); 
  }
  
  private DocDbBatchForOne successOutput(DocDbBatchForOne current, String msg) {
    return ImmutableDocDbBatchForOne.builder()
      .from(current)
      .status(BatchStatus.OK)
      .addMessages(ImmutableMessage.builder().text(msg).build())
      .build();
  }
  
  private DocDbBatchForOne failOutput(DocDbBatchForOne current, String msg, Throwable t) {
    log.error("Batch failed because of: " + msg, t);
    return ImmutableDocDbBatchForOne.builder()
        .from(current)
        .status(BatchStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(msg).build())
        .build(); 
  }
}
