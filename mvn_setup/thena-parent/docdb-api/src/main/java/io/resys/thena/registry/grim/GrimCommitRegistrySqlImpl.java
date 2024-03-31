package io.resys.thena.registry.grim;

import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimCommit;
import io.resys.thena.api.entities.grim.ImmutableGrimCommit;
import io.resys.thena.api.registry.grim.GrimCommitRegistry;
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
public class GrimCommitRegistrySqlImpl implements GrimCommitRegistry {
  private final TenantTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimCommit()).append(";").ln()
        .build()).build();
  }

  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getGrimCommit())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimCommit()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }

  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimCommit()).ln()
    .append("(").ln()
    .append("  commit_id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  parent_id VARCHAR(40),").ln()
    .append("  created_at TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
    .append("  commit_log TEXT NOT NULL,").ln()
    
    .append("  commit_author VARCHAR(255) NOT NULL,").ln()
    .append("  commit_message VARCHAR(255) NOT NULL").ln()
    
    .append(");").ln()
    

    .append("CREATE INDEX ").append(options.getGrimCommit()).append("_PARENT_INDEX")
    .append(" ON ").append(options.getGrimCommit()).append(" (parent_id);").ln()

    .append("CREATE INDEX ").append(options.getGrimCommit()).append("_AUTH_INDEX")
    .append(" ON ").append(options.getGrimCommit()).append(" (commit_author);").ln()
    

    // parent id, references self
    .append("ALTER TABLE ").append(options.getGrimCommit()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimCommit()).append("_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent_id)").ln()
    .append("  REFERENCES ").append(options.getGrimCommit()).append(" (commit_id);").ln()

    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
      .ln().append("--- constraints for").append(options.getGrimCommit()).ln()
     .append(createGrimCommitFk(options.getGrimAssignment()))
     .append(createGrimCommitFk(options.getGrimCommitTree()))
     .append(createGrimCommitFk(options.getGrimCommitViewer()))
     .append(createGrimCommitFk(options.getGrimLabel()))
     .append(createGrimCommitFk(options.getGrimMission()))
     .append(createGrimCommitFk(options.getGrimMissionData()))
     .append(createGrimCommitFk(options.getGrimMissionLabel()))
     .append(createGrimCommitFk(options.getGrimMissionLink()))
     .append(createGrimCommitFk(options.getGrimObjective()))
     .append(createGrimCommitFk(options.getGrimObjectiveGoal()))
     .append(createGrimCommitFk(options.getGrimRemark()))
    .build()).build();
  }


  @Override
  public Function<Row, GrimCommit> defaultMapper() {
    return (row) -> {
      
      return ImmutableGrimCommit.builder().build();
    };
  }
  
  private String createGrimCommitFk(String tableNameThatPointToCommits) {
    return new SqlStatement().ln()
        .append("ALTER TABLE ").append(tableNameThatPointToCommits).ln()
        .append("  ADD CONSTRAINT ").append(tableNameThatPointToCommits).append("_COMMIT_FK").ln()
        .append("  FOREIGN KEY (commit_id)").ln()
        .append("  REFERENCES ").append(options.getGrimCommit()).append(" (commit_id);").ln().ln()
        .build();
  }

}