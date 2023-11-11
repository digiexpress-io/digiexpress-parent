package io.resys.thena.docdb.store.sql.factories;

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
import io.resys.thena.docdb.store.sql.SqlBuilder;
import io.resys.thena.docdb.store.sql.statement.DefaultBlobSqlBuilder;
import io.resys.thena.docdb.store.sql.statement.DefaultCommitSqlBuilder;
import io.resys.thena.docdb.store.sql.statement.DefaultRefSqlBuilder;
import io.resys.thena.docdb.store.sql.statement.DefaultRepoSqlBuilder;
import io.resys.thena.docdb.store.sql.statement.DefaultTagSqlBuilder;
import io.resys.thena.docdb.store.sql.statement.DefaultTreeItemSqlBuilder;
import io.resys.thena.docdb.store.sql.statement.DefaultTreeSqlBuilder;
import io.resys.thena.docdb.store.sql.statement.DocBranchSqlBuilderImpl;
import io.resys.thena.docdb.store.sql.statement.DocCommitSqlBuilderImpl;
import io.resys.thena.docdb.store.sql.statement.DocLogSqlBuilderImpl;
import io.resys.thena.docdb.store.sql.statement.DocSqlBuilderImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SqlBuilderImpl implements SqlBuilder {
  protected final DbCollections ctx;

  @Override
  public RepoSqlBuilder repo() {
    return new DefaultRepoSqlBuilder(ctx);
  }
  @Override
  public GitRefSqlBuilder refs() {
    return new DefaultRefSqlBuilder(ctx);
  }
  @Override
  public GitTagSqlBuilder tags() {
    return new DefaultTagSqlBuilder(ctx);
  }
  @Override
  public GitBlobSqlBuilder blobs() {
    return new DefaultBlobSqlBuilder(ctx);
  }
  @Override
  public GitTreeSqlBuilder trees() {
    return new DefaultTreeSqlBuilder(ctx);
  }
  @Override
  public GitTreeItemSqlBuilder treeItems() {
    return new DefaultTreeItemSqlBuilder(ctx);
  }
  @Override
  public GitCommitSqlBuilder commits() {
    return new DefaultCommitSqlBuilder(ctx);
  }
  @Override
  public SqlBuilder withOptions(DbCollections options) {
    return new SqlBuilderImpl(options);
  }
  @Override
  public DocSqlBuilder docs() {
    return new DocSqlBuilderImpl(ctx);
  }
  @Override
  public DocLogSqlBuilder docLogs() {
    return new DocLogSqlBuilderImpl(ctx);
  }
  @Override
  public DocCommitSqlBuilder docCommits() {
    return new DocCommitSqlBuilderImpl(ctx);
  }
  @Override
  public DocBranchSqlBuilder docBranches() {
    return new DocBranchSqlBuilderImpl(ctx);
  }
}
