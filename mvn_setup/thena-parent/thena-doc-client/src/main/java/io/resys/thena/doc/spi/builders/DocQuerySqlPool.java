package io.resys.thena.doc.spi.builders;

import java.util.List;

/*-
 * #%L
 * thena-docdb-api
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

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.Doc.DocFilter;
import io.resys.thena.api.entities.doc.ImmutableDocFilter;
import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.doc.api.DocQueries.DocDeleteForMany;
import io.resys.thena.doc.api.DocQueries.DocQuery;
import io.resys.thena.doc.api.ImmutableDocDeleteForMany;
import io.resys.thena.doc.spi.datasource.DocRegistry;
import io.resys.thena.doc.spi.sql.DocRegistrySqlImpl;
import io.resys.thena.storesql.support.Execute;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
public class DocQuerySqlPool implements DocQuery {
  private final ThenaSqlDataSource wrapper;
  private final DocRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public DocQuerySqlPool(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.registry = new DocRegistrySqlImpl(dataSource.getRegistry());
    this.errorHandler = dataSource.getErrorHandler();
  }
  @Override
  public Uni<Doc> getById(String id) {
    final var sql = registry.docs().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("Doc byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.docs().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Doc> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'DOC' by 'id': '" + id + "'!", sql, e)));
  }
  @Override
  public Multi<Doc> findAll() {
    final var sql = registry.docs().findAll();
    if(log.isDebugEnabled()) {
      log.debug("Doc findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.docs().defaultMapper())
        .execute()
        .onItem()
        .transformToMulti((RowSet<Doc> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'DOC'!", sql, e)));
  }
  @Override
  public Multi<Doc> findAll(DocFilter filter) {
    final var sql = registry.docs().findAll(filter);
    if(log.isDebugEnabled()) {
      log.debug("Doc findAllByIds query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.docs().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<Doc> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'DOC' for filter: '" + filter + "'!", sql, e)));
  }
  
  
  @Override
  public Uni<DocDeleteForMany> deleteAll(List<String> inputIds) {
    
    RepoAssert.isTrue(this.wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    final var tx = wrapper.getClient();
    
    return findAll(ImmutableDocFilter.builder().docIds(inputIds).build())
        .collect().asList().onItem()
        .transformToUni(docsToDelete -> {
          
          RepoAssert.isTrue(docsToDelete.size() == inputIds.size(), () -> "Can't find all docs for delete!");
          final var ids = inputIds;
          
          final var start = ImmutableDocDeleteForMany.builder()
              .status(BatchStatus.OK)
              .log("")
              .repo(wrapper.getTenantContext().getTenant())
              .build();
          
          final var doc_delete_sql = registry.docs().deleteByDocId(ids);
          final var branches_delete_sql = registry.docBranches().deleteByDocId(ids);
          final var commands_delete_sql = registry.docCommands().deleteByDocId(ids);
          final var commits_delete_sql = registry.docCommits().deleteByDocId(ids);
          final var trees_delete_sql = registry.docCommitTrees().deleteByDocId(ids);
     
          
          final Uni<DocDeleteForMany> docs_delete_uni = Execute.apply(tx, doc_delete_sql).onItem()
              .transform(row -> successOutput(start, "Doc deleted, number of new entries: " + (row == null ? 0 : row.rowCount())))
              .onFailure().transform(e -> failOutput(start, "Failed to delete docs", e));

          
          final Uni<DocDeleteForMany> branches_delete_uni = Execute.apply(tx, branches_delete_sql).onItem()
              .transform(row -> successOutput(start, "Doc branches deleted, number of deleted entries: " + (row == null ? 0 : row.rowCount())))
              .onFailure().transform(e -> failOutput(start, "Failed to delete doc branches", e));
          
          final Uni<DocDeleteForMany> commands_delete_uni = Execute.apply(tx, commands_delete_sql).onItem()
              .transform(row -> successOutput(start, "Doc commands deleted, number of deleted entries: " + (row == null ? 0 : row.rowCount())))
              .onFailure().transform(e -> failOutput(start, "Failed to delete doc commands", e));

          final Uni<DocDeleteForMany> commits_delete_uni = Execute.apply(tx, commits_delete_sql).onItem()
              .transform(row -> successOutput(start, "Doc commits deleted, number of deleted entries: " + (row == null ? 0 : row.rowCount())))
              .onFailure().transform(e -> failOutput(start, "Failed to delete doc commits", e));

          final Uni<DocDeleteForMany> trees_delete_uni = Execute.apply(tx, trees_delete_sql).onItem()
              .transform(row -> successOutput(start, "Doc trees deleted, number of deleted entries: " + (row == null ? 0 : row.rowCount())))
              .onFailure().transform(e -> failOutput(start, "Failed to delete doc trees", e));


          
          return Uni.combine().all().unis(
                commands_delete_uni, 
                branches_delete_uni,
                trees_delete_uni,
                commits_delete_uni,
                docs_delete_uni
              )
              .asTuple()
              .onItem().transform(tuple -> merge(start,
                      tuple.getItem1(), 
                      tuple.getItem2()
              ))
              .onFailure(DocDeleteForManyException.class)
              .recoverWithUni((ex) -> {
                final var batchError = (DocDeleteForManyException) ex;
                return tx.rollback().onItem().transform(junk -> batchError.getBatch());
              });
        });
    
  }
  
  
  private DocDeleteForMany successOutput(DocDeleteForMany current, String msg) {
    return ImmutableDocDeleteForMany.builder()
      .from(current)
      .status(BatchStatus.OK)
      .addMessages(ImmutableMessage.builder().text(msg).build())
      .build();
  }
  private DocDeleteForManyException failOutput(DocDeleteForMany current, String msg, Throwable t) {
    log.error("Batch failed because of: " + msg, t);
    return new DocDeleteForManyException(ImmutableDocDeleteForMany.builder()
        .from(current)
        .status(BatchStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(msg).exception(t).build())
        .build()); 
  }
  
  
  private static class DocDeleteForManyException extends RuntimeException {
    private static final long serialVersionUID = -7251738425609399151L;
    private final DocDeleteForMany batch;
    
    public DocDeleteForManyException(DocDeleteForMany batch) {
      this.batch = batch;
    }
    public DocDeleteForMany getBatch() {
      return batch;
    }
  } 
  
  private DocDeleteForMany merge(DocDeleteForMany start, DocDeleteForMany ... current) {
    final var builder = ImmutableDocDeleteForMany.builder().from(start);
    final var log = new StringBuilder(start.getLog());
    var status = start.getStatus();
    for(DocDeleteForMany value : current) {
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


}
