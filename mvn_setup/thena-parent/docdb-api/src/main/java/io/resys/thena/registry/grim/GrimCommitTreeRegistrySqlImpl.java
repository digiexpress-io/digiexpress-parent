package io.resys.thena.registry.grim;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimCommitTree;
import io.resys.thena.api.entities.grim.GrimCommitTree.GrimCommitTreeOperation;
import io.resys.thena.api.entities.grim.ImmutableGrimCommitTree;
import io.resys.thena.api.registry.grim.GrimCommitTreeRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GrimCommitTreeRegistrySqlImpl implements GrimCommitTreeRegistry {
  private final TenantTableNames options;
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimCommitTree()).append(";").ln()
        .build()).build();
  }
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT tree.*, commit.mission_id as mission_id").ln()
        .append(" FROM ").append(options.getGrimCommitTree()).append(" as tree ").ln()
        .append(" RIGHT JOIN ").append(options.getGrimCommit()).append(" as commit").ln()
        .append(" ON(tree.commit_id = commit.commit_id)").ln()
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimCommitTree()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple findAllByCommitIds(List<String> commitId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimCommitTree()).ln()
        .append("  WHERE (commit_id = ANY($1))").ln() 
        .build())
        .props(Tuple.of(commitId.toArray()))
        .build();    
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimCommitTree()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    
    .append("  operation_type VARCHAR(40),").ln()
    .append("  body_after JSONB,").ln()
    .append("  body_before JSONB").ln()
    .append(");").ln()
    
    
    .build()).build();
  }
  @Override
  public SqlTupleList insertAll(Collection<GrimCommitTree> commits) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getGrimCommitTree()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()
        .append("  operation_type,").ln()
        .append("  body_after,").ln()
        .append("  body_before)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5)").ln()
        .build())
        .props(commits.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getOperationType().name(),
                doc.getBodyAfter(),
                doc.getBodyBefore(),
             }))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
    .ln().append("--- constraints for").append(options.getGrimCommitTree()).ln()
    .build()).build();
  }

  @Override
  public Function<Row, GrimCommitTree> defaultMapper() {
    return (row) -> {
      
      return ImmutableGrimCommitTree.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .missionId(row.getString("mission_id"))
          .operationType(GrimCommitTreeOperation.valueOf(row.getString("operation_type")))
          .bodyBefore(row.getJsonObject("body_before"))
          .bodyAfter(row.getJsonObject("body_after"))
          .build();
    };
  }

}