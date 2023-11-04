package io.resys.thena.docdb.sql.factories;

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

import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.sql.SqlBuilder;
import io.resys.thena.docdb.sql.statement.DefaultBlobSqlBuilder;
import io.resys.thena.docdb.sql.statement.DefaultCommitSqlBuilder;
import io.resys.thena.docdb.sql.statement.DefaultRefSqlBuilder;
import io.resys.thena.docdb.sql.statement.DefaultRepoSqlBuilder;
import io.resys.thena.docdb.sql.statement.DefaultTagSqlBuilder;
import io.resys.thena.docdb.sql.statement.DefaultTreeItemSqlBuilder;
import io.resys.thena.docdb.sql.statement.DefaultTreeSqlBuilder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SqlBuilderImpl implements SqlBuilder {
  protected final DbCollections ctx;

  @Override
  public RepoSqlBuilder repo() {
    return new DefaultRepoSqlBuilder(ctx);
  }
  @Override
  public RefSqlBuilder refs() {
    return new DefaultRefSqlBuilder(ctx);
  }
  @Override
  public TagSqlBuilder tags() {
    return new DefaultTagSqlBuilder(ctx);
  }
  @Override
  public BlobSqlBuilder blobs() {
    return new DefaultBlobSqlBuilder(ctx);
  }
  @Override
  public TreeSqlBuilder trees() {
    return new DefaultTreeSqlBuilder(ctx);
  }
  @Override
  public TreeItemSqlBuilder treeItems() {
    return new DefaultTreeItemSqlBuilder(ctx);
  }
  @Override
  public CommitSqlBuilder commits() {
    return new DefaultCommitSqlBuilder(ctx);
  }
  @Override
  public SqlBuilder withOptions(DbCollections options) {
    return new SqlBuilderImpl(options);
  }
}
