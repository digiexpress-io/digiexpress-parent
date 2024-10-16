package io.resys.thena.registry.grim;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimMissionLabel;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionLabel;
import io.resys.thena.api.registry.grim.GrimMissionFilter;
import io.resys.thena.api.registry.grim.GrimMissionLabelRegistry;
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
public class GrimMissionLabelRegistrySqlImpl implements GrimMissionLabelRegistry {
  private final TenantTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimMissionLabel()).append(";").ln()
        .build()).build();
  }
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getGrimMissionLabel())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimMissionLabel()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<GrimMissionLabel> labels) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getGrimMissionLabel()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()

        .append("  mission_id,").ln()
        .append("  objective_id,").ln()
        .append("  goal_id,").ln()
        .append("  remark_id,").ln()
        
        .append("  label_type,").ln()
        .append("  label_value,").ln()
        .append("  label_body)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9)").ln()
        .build())
        .props(labels.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getMissionId(),
                doc.getRelation() == null ? null : doc.getRelation().getObjectiveId(),
                doc.getRelation() == null ? null : doc.getRelation().getObjectiveGoalId(),
                doc.getRelation() == null ? null : doc.getRelation().getRemarkId(),
                    
                doc.getLabelType(),
                doc.getLabelValue(),
                doc.getLabelBody()
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimMissionLabel()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()

    .append("  label_type VARCHAR(100) NOT NULL,").ln()
    .append("  label_value VARCHAR(255) NOT NULL,").ln()
    .append("  label_body JSONB,").ln()

    .append("  mission_id VARCHAR(40) NOT NULL,").ln()
    .append("  objective_id VARCHAR(40),").ln()
    .append("  goal_id VARCHAR(40),").ln()
    .append("  remark_id VARCHAR(40),").ln()
    
    .append("  UNIQUE NULLS NOT DISTINCT(mission_id, objective_id, goal_id, remark_id, label_type, label_value)").ln()    
    .append(");").ln()

    
    .append("CREATE INDEX ").append(options.getGrimMissionLabel()).append("_MISSION_INDEX")
    .append(" ON ").append(options.getGrimMissionLabel()).append(" (mission_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionLabel()).append("_OBJECTIVE_INDEX")
    .append(" ON ").append(options.getGrimMissionLabel()).append(" (objective_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionLabel()).append("_GOAL_INDEX")
    .append(" ON ").append(options.getGrimMissionLabel()).append(" (goal_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionLabel()).append("_REMARK_INDEX")
    .append(" ON ").append(options.getGrimMissionLabel()).append(" (remark_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionLabel()).append("_LABEL_INDEX")
    .append(" ON ").append(options.getGrimMissionLabel()).append(" (label_value);").ln()
    
    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for").append(options.getGrimMissionLabel()).ln()
        .append("ALTER TABLE ").append(options.getGrimMissionLabel()).ln()
        .append("  ADD CONSTRAINT ").append(options.getGrimMissionLabel()).append("_MISSION_FK").ln()
        .append("  FOREIGN KEY (mission_id)").ln()
        .append("  REFERENCES ").append(options.getGrimMission()).append(" (id);").ln().ln()
        
        .append("ALTER TABLE ").append(options.getGrimMissionLabel()).ln()
        .append("  ADD CONSTRAINT ").append(options.getGrimMissionLabel()).append("_OBJECTIVE_FK").ln()
        .append("  FOREIGN KEY (objective_id)").ln()
        .append("  REFERENCES ").append(options.getGrimObjective()).append(" (id);").ln().ln()
        
        .append("ALTER TABLE ").append(options.getGrimMissionLabel()).ln()
        .append("  ADD CONSTRAINT ").append(options.getGrimMissionLabel()).append("_GOAL_FK").ln()
        .append("  FOREIGN KEY (goal_id)").ln()
        .append("  REFERENCES ").append(options.getGrimObjectiveGoal()).append(" (id);").ln().ln()
        
        .append("ALTER TABLE ").append(options.getGrimMissionLabel()).ln()
        .append("  ADD CONSTRAINT ").append(options.getGrimMissionLabel()).append("_REMARK_FK").ln()
        .append("  FOREIGN KEY (remark_id)").ln()
        .append("  REFERENCES ").append(options.getGrimRemark()).append(" (id);").ln().ln()
        
    .build()).build();
  }


  @Override
  public Function<Row, GrimMissionLabel> defaultMapper() {
    return (row) -> {
      final var objectiveId = row.getString("objective_id");
      final var goalId = row.getString("goal_id");
      final var remarkId = row.getString("remark_id");
      
      return ImmutableGrimMissionLabel.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .missionId(row.getString("mission_id"))
          .labelType(row.getString("label_type"))
          .labelValue(row.getString("label_value"))
          .labelBody(row.getJsonObject("label_body"))
          .relation(GrimRegistrySqlImpl.toRelations(objectiveId, goalId, remarkId))
          .build();
    };
  }
  @Override
  public SqlTuple findAllByMissionIds(GrimMissionFilter filter) {
    final var where = new GrimMissionSqlFilterBuilder(options).where(filter);
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT mission_label.* ").ln()
        .append("  FROM ").append(options.getGrimMissionLabel()).append(" as mission_label").ln()
        
        .append("  LEFT JOIN ").append(options.getGrimMission()).append(" as mission")
        .append("  ON(mission_label.mission_id = mission.id)")
        .append(where.getValue()) 
        .build())
        .props(where.getProps())
        .build();
  }
  @Override
  public SqlTupleList deleteAll(Collection<GrimMissionLabel> labels) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getGrimMissionLabel())
        .append(" WHERE id = $1")
        .build())
        .props(labels.stream()
            .map(doc -> Tuple.from(new Object[]{doc.getId()}))
            .collect(Collectors.toList()))
        .build();
  }

}