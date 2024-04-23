package io.resys.thena.registry.doc;

import java.util.Arrays;
import java.util.function.Function;

import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.ImmutableDocCommands;
import io.resys.thena.api.registry.doc.DocCommandsRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocCommandsRegistrySqlImpl implements DocCommandsRegistry {
  private final TenantTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimCommands()).append(";").ln()
        .build()).build();
  }

  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT commands.*, commits.created_at as created_at ")
        .append(" FROM ").append(options.getGrimCommands()).append(" as commands")
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as commits").ln()
        .append(" ON(commands.commit_id = commits.commit_id)").ln()
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT commands.*, commits.created_at as created_at ")
        .append(" FROM ").append(options.getGrimCommands()).append(" as commands")
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as commits").ln()
        .append(" ON(commands.commit_id = commits.commit_id)").ln()
        .append(" WHERE (commands.id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimCommands()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  mission_id VARCHAR(40) NOT NULL,").ln()
    .append("  commands JSONB[] NOT NULL").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getGrimCommands()).append("_MISSION_INDEX")
    .append(" ON ").append(options.getGrimCommands()).append(" (mission_id);").ln()
 
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
      .ln().append("--- constraints for").append(options.getGrimCommands()).ln()
      
      .append("ALTER TABLE ").append(options.getGrimCommands()).ln()
      .append("  ADD CONSTRAINT ").append(options.getGrimCommands()).append("_MISSION_FK").ln()
      .append("  FOREIGN KEY (mission_id)").ln()
      .append("  REFERENCES ").append(options.getGrimMission()).append(" (id);").ln().ln()
      
    .build()).build();
  }


  @Override
  public Function<Row, DocCommands> defaultMapper() {
    return (row) -> {
      return ImmutableDocCommands.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .missionId(row.getString("mission_id"))
          .createdAt(row.getOffsetDateTime("created_at"))
          .commands(Arrays.asList(row.getArrayOfJsonObjects("commands")))
          .build();
    };
  }

}