package io.resys.thena.docdb.sql;

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
import io.resys.thena.docdb.sql.builders.BlobQuerySqlPool;
import io.resys.thena.docdb.sql.builders.CommitQuerySqlPool;
import io.resys.thena.docdb.sql.builders.RefQuerySqlPool;
import io.resys.thena.docdb.sql.builders.TagQuerySqlPool;
import io.resys.thena.docdb.sql.builders.TreeQuerySqlPool;
import io.resys.thena.docdb.sql.support.SqlClientWrapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClientQuerySqlPool implements ClientQuery {
  
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;
  
  @Override
  public TagQuery tags() {
    return new TagQuerySqlPool(wrapper.getClient(), sqlMapper, sqlBuilder, errorHandler);
  }

  @Override
  public CommitQuery commits() {
    return new CommitQuerySqlPool(wrapper.getClient(), sqlMapper, sqlBuilder, errorHandler);
  }

  @Override
  public RefQuery refs() {
    return new RefQuerySqlPool(wrapper.getClient(), sqlMapper, sqlBuilder, errorHandler);
  }

  @Override
  public TreeQuery trees() {
    return new TreeQuerySqlPool(wrapper.getClient(), sqlMapper, sqlBuilder, errorHandler);
  }

  @Override
  public BlobQuery blobs() {
    return new BlobQuerySqlPool(wrapper.getClient(), sqlMapper, sqlBuilder, errorHandler);
  }
}
