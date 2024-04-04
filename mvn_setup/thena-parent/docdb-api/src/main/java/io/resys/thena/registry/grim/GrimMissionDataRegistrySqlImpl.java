package io.resys.thena.registry.grim;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimMissionData;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionData;
import io.resys.thena.api.registry.grim.GrimMissionDataRegistry;
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
public class GrimMissionDataRegistrySqlImpl implements GrimMissionDataRegistry {
  private final TenantTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimMissionData()).append(";").ln()
        .build()).build();
  }
  @Override
  public SqlTuple findAllByMissionIds(Collection<String> id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ").ln()
        .append(" mission_data.*,").ln()
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at").ln()
        
        .append(" FROM ").append(options.getGrimMissionData()).append(" as mission_data ").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = mission_data.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = mission_data.created_commit_id)").ln()
        
        .append(" WHERE mission_data.mission_id = ANY($1)").ln() 
        .build())
        .props(Tuple.of(id.toArray()))
        .build();
  }
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT ").ln()
        .append(" mission_data.*,").ln()
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at").ln()
        
        .append(" FROM ").append(options.getGrimMissionData()).append(" as mission_data ").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = mission_data.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = mission_data.created_commit_id)").ln()
        
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ").ln()
        .append(" mission_data.*,").ln()
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at").ln()
        
        .append(" FROM ").append(options.getGrimMissionData()).append(" as mission_data ").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = mission_data.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = mission_data.created_commit_id)").ln()
        
        .append(" WHERE mission_data.id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<GrimMissionData> labels) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getGrimMissionData()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()
        .append("  created_commit_id,").ln()
        .append("  mission_id,").ln()
        .append("  objective_id,").ln()
        .append("  goal_id,").ln()
        .append("  remark_id,").ln()
        
        .append("  title,").ln()
        .append("  description,").ln()
        .append("  data_extension)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)").ln()
        .build())
        .props(labels.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getCreatedWithCommitId(),
                doc.getMissionId(),
                doc.getRelation() == null ? null : doc.getRelation().getObjectiveId(),
                doc.getRelation() == null ? null : doc.getRelation().getObjectiveGoalId(),
                doc.getRelation() == null ? null : doc.getRelation().getRemarkId(),
                
                doc.getTitle(),
                doc.getDescription(),
                doc.getDataExtension()
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTupleList updateAll(Collection<GrimMissionData> data) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getGrimMissionData())
        .append(" SET").ln()
        .append("  commit_id = $1,").ln()
        .append("  title = $2,").ln()
        .append("  description = $3,").ln()
        .append("  data_extension = $4").ln()
        .append(" WHERE id = $5")
        .build())
        .props(data.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getCommitId(),
                doc.getTitle(),
                doc.getDescription(),
                doc.getDataExtension(),
                doc.getId(), 
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimMissionData()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  created_commit_id VARCHAR(40) NOT NULL,").ln()
    
    .append("  title TEXT NOT NULL,").ln()
    .append("  description TEXT NOT NULL,").ln()
    .append("  data_extension JSONB,").ln()
    
    .append("  mission_id VARCHAR(40) NOT NULL,").ln()
    .append("  objective_id VARCHAR(40),").ln()
    .append("  goal_id VARCHAR(40),").ln()
    .append("  remark_id VARCHAR(40),").ln()
    
    .append("  UNIQUE NULLS NOT DISTINCT(mission_id, objective_id, goal_id, remark_id)").ln()
    
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionData()).append("_CREATED_INDEX")
    .append(" ON ").append(options.getGrimMissionData()).append(" (created_commit_id);").ln()

    .append("CREATE INDEX ").append(options.getGrimMissionData()).append("_MISSION_INDEX")
    .append(" ON ").append(options.getGrimMissionData()).append(" (mission_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionData()).append("_OBJECTIVE_INDEX")
    .append(" ON ").append(options.getGrimMissionData()).append(" (objective_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionData()).append("_GOAL_INDEX")
    .append(" ON ").append(options.getGrimMissionData()).append(" (goal_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionData()).append("_REMARK_INDEX")
    .append(" ON ").append(options.getGrimMissionData()).append(" (remark_id);").ln()
    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for").append(options.getGrimMissionData()).ln()
        .append("ALTER TABLE ").append(options.getGrimMissionData()).ln()
        .append("  ADD CONSTRAINT ").append(options.getGrimMissionLabel()).append("_MISSION_FK").ln()
        .append("  FOREIGN KEY (mission_id)").ln()
        .append("  REFERENCES ").append(options.getGrimMission()).append(" (id);").ln().ln()
        
        .append("ALTER TABLE ").append(options.getGrimMissionData()).ln()
        .append("  ADD CONSTRAINT ").append(options.getGrimMissionData()).append("_OBJECTIVE_FK").ln()
        .append("  FOREIGN KEY (objective_id)").ln()
        .append("  REFERENCES ").append(options.getGrimObjective()).append(" (id);").ln().ln()
        
        .append("ALTER TABLE ").append(options.getGrimMissionData()).ln()
        .append("  ADD CONSTRAINT ").append(options.getGrimMissionData()).append("_GOAL_FK").ln()
        .append("  FOREIGN KEY (goal_id)").ln()
        .append("  REFERENCES ").append(options.getGrimObjectiveGoal()).append(" (id);").ln().ln()
        
        .append("ALTER TABLE ").append(options.getGrimMissionData()).ln()
        .append("  ADD CONSTRAINT ").append(options.getGrimMissionData()).append("_REMARK_FK").ln()
        .append("  FOREIGN KEY (remark_id)").ln()
        .append("  REFERENCES ").append(options.getGrimRemark()).append(" (id);").ln().ln()

    .build()).build();
  }


  @Override
  public Function<Row, GrimMissionData> defaultMapper() {
    return (row) -> {
      final var objectiveId = row.getString("objective_id");
      final var goalId = row.getString("goal_id");
      final var remarkId = row.getString("remark_id");
      
      return ImmutableGrimMissionData.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .missionId(row.getString("mission_id"))
          
          .updatedAt(row.getOffsetDateTime("updated_at"))
          .createdAt(row.getOffsetDateTime("created_at"))
          .createdWithCommitId(row.getString("created_commit_id"))
          
          .relation(GrimRegistrySqlImpl.toRelations(objectiveId, goalId, remarkId))
          .title(row.getString("title"))
          .description(row.getString("description"))
          .dataExtension(row.getJsonObject("data_extension"))
          .build();
    };
  }

}