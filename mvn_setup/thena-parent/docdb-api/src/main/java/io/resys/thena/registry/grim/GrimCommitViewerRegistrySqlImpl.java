package io.resys.thena.registry.grim;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.entities.grim.ImmutableGrimCommitViewer;
import io.resys.thena.api.registry.grim.GrimCommitViewerRegistry;
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
public class GrimCommitViewerRegistrySqlImpl implements GrimCommitViewerRegistry {
  private final TenantTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimCommitViewer()).append(";").ln()
        .build()).build();
  }
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getGrimCommitViewer())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimCommitViewer()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<GrimCommitViewer> commits) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getGrimCommitViewer()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()
        .append("  object_id,").ln()
        .append("  object_type,").ln()
        .append("  used_by,").ln()
        .append("  used_for,").ln()
        .append("  created_at)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7)").ln()
        .build())
        .props(commits.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getObjectId(),
                doc.getObjectType(),
                doc.getUsedBy(),
                doc.getUsedFor(),
                doc.getCreatedAt()
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimCommitViewer()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  object_id VARCHAR(40) NOT NULL,").ln()
    .append("  object_type VARCHAR(255) NOT NULL,").ln()
    .append("  used_by VARCHAR(255) NOT NULL,").ln()
    .append("  used_for VARCHAR(255) NOT NULL,").ln()
    .append("  created_at TIMESTAMP WITH TIME ZONE NOT NULL").ln()
    .append(");").ln()
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
    .ln().append("--- constraints for").append(options.getGrimCommitViewer()).ln()
    .build()).build();
  }


  @Override
  public Function<Row, GrimCommitViewer> defaultMapper() {
    return (row) -> {
      
      return ImmutableGrimCommitViewer.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .build();
    };
  }
  @Override
  public SqlTuple findAllByMissionIds(Collection<String> id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimCommitViewer()).ln()
        .append("  WHERE (object_id = ANY($1))").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
}