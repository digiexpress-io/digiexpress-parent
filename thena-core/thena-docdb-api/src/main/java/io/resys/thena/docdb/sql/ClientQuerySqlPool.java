package io.resys.thena.docdb.sql;

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

import io.resys.thena.docdb.spi.ClientQuery;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.sql.builders.BlobHistoryQuerySqlPool;
import io.resys.thena.docdb.sql.builders.BlobQuerySqlPool;
import io.resys.thena.docdb.sql.builders.CommitQuerySqlPool;
import io.resys.thena.docdb.sql.builders.RefQuerySqlPool;
import io.resys.thena.docdb.sql.builders.TagQuerySqlPool;
import io.resys.thena.docdb.sql.builders.TreeQuerySqlPool;
import io.resys.thena.docdb.sql.support.SqlClientWrapper;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ClientQuerySqlPool implements ClientQuery {
  
  protected final ClientQuerySqlContext context;
  
  @Value.Immutable
  public interface ClientQuerySqlContext {
    SqlClientWrapper getWrapper();
    SqlMapper getMapper();
    SqlBuilder getBuilder();
    ErrorHandler getErrorHandler();
  }
  
  @Override
  public TagQuery tags() {
    return new TagQuerySqlPool(context.getWrapper().getClient(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }

  @Override
  public CommitQuery commits() {
    return new CommitQuerySqlPool(context.getWrapper().getClient(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }

  @Override
  public RefQuery refs() {
    return new RefQuerySqlPool(context.getWrapper().getClient(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }

  @Override
  public TreeQuery trees() {
    return new TreeQuerySqlPool(context.getWrapper().getClient(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }

  @Override
  public BlobQuery blobs() {
    return new BlobQuerySqlPool(context.getWrapper().getClient(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }
  
  @Override
  public BlobHistoryQuery blobHistory() {
    return new BlobHistoryQuerySqlPool(context);
  }
}
