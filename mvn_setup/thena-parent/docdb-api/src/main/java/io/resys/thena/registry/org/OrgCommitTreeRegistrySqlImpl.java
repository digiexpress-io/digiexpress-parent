package io.resys.thena.registry.org;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.org.OrgCommitTree;
import io.resys.thena.api.registry.org.OrgCommitTreeRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgCommitTreeRegistrySqlImpl implements OrgCommitTreeRegistry {
  private final TenantTableNames options;
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getOrgCommitTrees())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgCommitTrees()).ln()
        .append("  WHERE id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<OrgCommitTree> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgCommitTrees())
        .append(" (id, commit_id, parent_commit_id, actor_id, actor_type, value) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getParentCommitId(), doc.getActorId(), doc.getActorType(), doc.getValue() }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTuple findByCommmitId(String commitId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgCommitTrees()).ln()
        .append("  WHERE commit_id = $1").ln() 
        .build())
        .props(Tuple.of(commitId))
        .build();
  }
  @Override
  public Function<Row, OrgCommitTree> defaultMapper() {
    throw new RuntimeException("Not implemented");
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value("").build();
  }
  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value("").build();
  }
  @Override
  public Sql dropTable() {
    return ImmutableSql.builder().value("").build();
  }

}
