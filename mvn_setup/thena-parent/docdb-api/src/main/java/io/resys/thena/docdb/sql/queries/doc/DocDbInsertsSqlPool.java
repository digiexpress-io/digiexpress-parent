package io.resys.thena.docdb.sql.queries.doc;


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
import io.resys.thena.docdb.spi.ImmutableDocBatch;
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
  public Uni<DocBatch> batch(DocBatch output) {
    RepoAssert.isTrue(this.wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    final var tx = wrapper.getClient();
    
    final var docsInsert = sqlBuilder.docs().insertOne(output.getDoc());
    final var commitsInsert = sqlBuilder.docCommits().insertOne(output.getDocCommit());
    final var branchInsert = sqlBuilder.docBranches().insertOne(output.getDocBranch());
    final var logsInsert = output.getDocLogs().map(log -> sqlBuilder.docLogs().insertOne(log));
    
    
    final Uni<DocBatch> docsUni = Execute.apply(tx, docsInsert).onItem()
        .transform(row -> successOutput(output, "Doc saved, number of new entries: " + row.rowCount()))
        .onFailure().recoverWithItem(e -> failOutput(output, "Failed to create docs", e));
    
    final Uni<DocBatch> commitUni = Execute.apply(tx, commitsInsert).onItem()
      .transform(row -> successOutput(output, "Commit saved, number of new entries: " + row.rowCount()))
      .onFailure().recoverWithItem(e -> failOutput(output, "Failed to save commit \r\n" + output.getDocCommit(), e));

    final Uni<DocBatch> branchUni = Execute.apply(tx, branchInsert).onItem()
        .transform(row -> successOutput(output, "Branch saved, number of new entries: " + row.rowCount()))
        .onFailure().recoverWithItem(e -> failOutput(output, "Failed to save branch", e));
    
    
    final Uni<DocBatch> logUni;
    if(logsInsert.isEmpty()) {
      logUni = Uni.createFrom().item(successOutput(output, "Commit has no log, skipping log entry"));    
    } else {
      logUni = Execute.apply(tx, logsInsert.get()).onItem()
          .transform(row -> successOutput(output, "Commit log saved, number of new entries: " + row.rowCount()))
          .onFailure().recoverWithItem(e -> failOutput(output, "Failed to save  commit log", e)); 
    }
    
    return Uni.combine().all().unis(docsUni, commitUni, branchUni, logUni).asTuple()
        .onItem().transform(tuple -> merge(output, 
            tuple.getItem1(), 
            tuple.getItem2(), 
            tuple.getItem3(), 
            tuple.getItem4()
        ));
  }

  
  private DocBatch merge(DocBatch start, DocBatch ... current) {
    final var builder = ImmutableDocBatch.builder().from(start);
    final var log = new StringBuilder(start.getLog().getText());
    var status = start.getStatus();
    for(DocBatch value : current) {
      if(status != BatchStatus.ERROR) {
        status = value.getStatus();
      }
      log.append("\r\n\r\n").append(value.getLog());
      builder.addAllMessages(value.getMessages());
    }
    
    return builder.status(status).build();
  }
  
  private DocBatch successOutput(DocBatch current, String msg) {
    return ImmutableDocBatch.builder()
      .from(current)
      .status(BatchStatus.OK)
      .addMessages(ImmutableMessage.builder().text(msg).build())
      .build();
  }
  
  private DocBatch failOutput(DocBatch current, String msg, Throwable t) {
    log.error("Batch failed because of: " + msg, t);
    return ImmutableDocBatch.builder()
        .from(current)
        .status(BatchStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(msg).build())
        .build(); 
  }
}
