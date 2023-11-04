package io.resys.thena.docdb.sql.factories;

import org.immutables.value.Value;

/*-
 * #%L
 * thena-docdb-mongo
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

import io.resys.thena.docdb.spi.GitDbQueries;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.sql.SqlBuilder;
import io.resys.thena.docdb.sql.SqlMapper;
import io.resys.thena.docdb.sql.queries.git.GitBlobHistoryQuerySqlPool;
import io.resys.thena.docdb.sql.queries.git.GitBlobQuerySqlPool;
import io.resys.thena.docdb.sql.queries.git.GitCommitQuerySqlPool;
import io.resys.thena.docdb.sql.queries.git.GitRefQuerySqlPool;
import io.resys.thena.docdb.sql.queries.git.GitTagQuerySqlPool;
import io.resys.thena.docdb.sql.queries.git.GitTreeQuerySqlPool;
import io.resys.thena.docdb.sql.support.SqlClientWrapper;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ClientQuerySqlPool implements GitDbQueries {
  
  protected final ClientQuerySqlContext context;
  
  @Value.Immutable
  public interface ClientQuerySqlContext {
    SqlClientWrapper getWrapper();
    SqlMapper getMapper();
    SqlBuilder getBuilder();
    ErrorHandler getErrorHandler();
  }
  
  @Override
  public GitTagQuery tags() {
    return new GitTagQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }

  @Override
  public GitCommitQuery commits() {
    return new GitCommitQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }

  @Override
  public GitRefQuery refs() {
    return new GitRefQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }

  @Override
  public GitTreeQuery trees() {
    return new GitTreeQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }

  @Override
  public GitBlobQuery blobs() {
    return new GitBlobQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }
  
  @Override
  public GitBlobHistoryQuery blobHistory() {
    return new GitBlobHistoryQuerySqlPool(context);
  }
}
