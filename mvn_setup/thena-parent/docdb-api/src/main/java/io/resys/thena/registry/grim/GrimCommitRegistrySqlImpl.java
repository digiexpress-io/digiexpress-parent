package io.resys.thena.registry.grim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimCommit;
import io.resys.thena.api.entities.grim.ImmutableGrimCommit;
import io.resys.thena.api.registry.grim.GrimCommitRegistry;
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
  public SqlTupleList insertAll(Collection<GrimCommit> commits) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getGrimCommit()).ln()
        .append(" (commit_id,").ln()
        .append("  parent_id,").ln()
        .append("  mission_id,").ln()
        .append("  created_at,").ln()
        .append("  commit_log, ").ln()
        .append("  commit_author, ").ln()
        .append("  commit_message)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7)").ln()
        .build())
        .props(commits.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getCommitId(), 
                doc.getParentCommitId(),
                doc.getMissionId(),
                doc.getCreatedAt(),
                doc.getCommitLog(),
                doc.getCommitAuthor(),
                doc.getCommitMessage(),
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimCommit()).ln()
    .append("(").ln()
    .append("  commit_id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  parent_id VARCHAR(40),").ln()
    .append("  mission_id VARCHAR(40),").ln()
    .append("  created_at TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
    .append("  commit_log TEXT NOT NULL,").ln()
    
    .append("  commit_author VARCHAR(255) NOT NULL,").ln()
    .append("  commit_message VARCHAR(255) NOT NULL").ln()
    
    .append(");").ln()
    

    .append("CREATE INDEX ").append(options.getGrimCommit()).append("_PARENT_INDEX")
    .append(" ON ").append(options.getGrimCommit()).append(" (parent_id);").ln()

    .append("CREATE INDEX ").append(options.getGrimCommit()).append("_MISSION_INDEX")
    .append(" ON ").append(options.getGrimCommit()).append(" (mission_id);").ln()
    
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
     .append(createGrimCommitFk(options.getGrimMissionData()))
     .append(createGrimCommitFk(options.getGrimMissionLabel()))
     .append(createGrimCommitFk(options.getGrimMissionLink()))
     .append(createGrimCommitFk(options.getGrimObjective()))
     .append(createGrimCommitFk(options.getGrimObjectiveGoal()))
     .append(createGrimCommitFk(options.getGrimRemark()))
     .append(createGrimCommitFk(options.getGrimCommands()))
     
     // mission table
     .append(createGrimCommitFk(options.getGrimMission()))
     .append(createGrimCommitFk(options.getGrimMission(), "created_commit_id"))
     .append(createGrimCommitFk(options.getGrimMissionLink(), "created_commit_id"))
     .append(createGrimCommitFk(options.getGrimMission(), "updated_tree_commit_id"))
     
     
    .build()).build();
  }


  @Override
  public Function<Row, GrimCommit> defaultMapper() {
    return (row) -> {
      
      return ImmutableGrimCommit.builder()
          .commitId(row.getString("commit_id"))
          .missionId(row.getString("mission_id"))

          .parentCommitId(row.getString("parent_id"))
          .createdAt(row.getOffsetDateTime("created_at"))
          .commitLog(row.getString("commit_log"))
          .commitAuthor(row.getString("commit_author"))
          .commitMessage(row.getString("commit_message"))
          
          .build();
    };
  }
  private String createGrimCommitFk(String tableNameThatPointToCommits, String column) {
    return new SqlStatement().ln()
        .append("ALTER TABLE ").append(tableNameThatPointToCommits).ln()
        .append("  ADD CONSTRAINT ").append(tableNameThatPointToCommits).append("_").append(column.toUpperCase()).append("_FK").ln()
        .append("  FOREIGN KEY (" + column + ")").ln()
        .append("  REFERENCES ").append(options.getGrimCommit()).append(" (commit_id);").ln().ln()
        .build();
  }
  private String createGrimCommitFk(String tableNameThatPointToCommits) {
    return new SqlStatement().ln()
        .append("ALTER TABLE ").append(tableNameThatPointToCommits).ln()
        .append("  ADD CONSTRAINT ").append(tableNameThatPointToCommits).append("_COMMIT_FK").ln()
        .append("  FOREIGN KEY (commit_id)").ln()
        .append("  REFERENCES ").append(options.getGrimCommit()).append(" (commit_id);").ln().ln()
        .build();
  }

  @Override
  public SqlTuple findAllByIds(Collection<String> id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimCommit()).ln()
        .append("  WHERE (id = ANY($1))").ln() 
        .build())
        .props(Tuple.from(new ArrayList<>(id)))
        .build();
  }

  @Override
  public SqlTuple findAllByMissionIds(Collection<String> id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimCommit()).ln()
        .append("  WHERE (mission_id = ANY($1))").ln() 
        .build())
        .props(Tuple.of(id.toArray()))
        .build();
  }

}