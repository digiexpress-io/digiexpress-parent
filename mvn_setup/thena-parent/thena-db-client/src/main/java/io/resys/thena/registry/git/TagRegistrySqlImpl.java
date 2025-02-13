package io.resys.thena.registry.git;

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

import java.time.LocalDateTime;
import java.util.function.Function;

import io.resys.thena.api.entities.git.ImmutableTag;
import io.resys.thena.api.entities.git.Tag;
import io.resys.thena.api.registry.git.TagRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TagRegistrySqlImpl implements TagRegistry {
  
  private final TenantTableNames options;
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTags())
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return getByName(id);
  }
  @Override
  public ThenaSqlClient.SqlTuple getByName(String name) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTags())
        .append(" WHERE id = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple deleteByName(String name) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getTags())
        .append(" WHERE id = $1")
        .build())
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public ThenaSqlClient.Sql getFirst() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTags())
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple insertOne(Tag newTag) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getTags())
        .append(" (id, commit, datetime, author, message) VALUES($1, $2, $3, $4, $5)")
        .build())
        .props(Tuple.of(newTag.getName(), newTag.getCommit(), newTag.getDateTime().toString(), newTag.getAuthor(), newTag.getMessage()))
        .build();
  }
  
  @Override
  public Function<Row, Tag> defaultMapper() {
    return TagRegistrySqlImpl::tag;
  }
  private static Tag tag(Row row) {
    return ImmutableTag.builder()
        .author(row.getString("author"))
        .dateTime(LocalDateTime.parse(row.getString("datetime")))
        .message(row.getString("message"))
        .commit(row.getString("commit"))
        .name(row.getString("id"))
        .build();
  }
  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
      .append("CREATE TABLE ").append(options.getTags()).ln()
      .append("(").ln()
      .append("  id VARCHAR(40) PRIMARY KEY,").ln()
      .append("  commit VARCHAR(40) NOT NULL,").ln()
      .append("  datetime VARCHAR(29) NOT NULL,").ln()
      .append("  author VARCHAR(40) NOT NULL,").ln()
      .append("  message VARCHAR(100) NOT NULL").ln()
      .append(");").ln()
      .build())
      .build();
  }
  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder()
        .value(new SqlStatement().ln()
        .append("ALTER TABLE ").append(options.getTags()).ln()
        .append("  ADD CONSTRAINT ").append(options.getTags()).append("_TAG_COMMIT_FK").ln()
        .append("  FOREIGN KEY (commit)").ln()
        .append("  REFERENCES ").append(options.getCommits()).append(" (id);").ln()
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getTags()).append(";").ln()
        .build()).build();
  }
}
