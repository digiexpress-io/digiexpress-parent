package io.resys.thena.registry.grim;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionLink;
import io.resys.thena.api.registry.grim.GrimMissionLinkRegistry;
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
public class GrimMissionLinkRegistrySqlImpl implements GrimMissionLinkRegistry {
  private final TenantTableNames options;
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimMissionLink()).append(";").ln()
        .build()).build();
  }
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getGrimMissionLink())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimMissionLink()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<GrimMissionLink> links) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getGrimMissionLink()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()

        .append("  mission_id,").ln()
        .append("  objective_id,").ln()
        .append("  goal_id,").ln()
        .append("  remark_id,").ln()
        
        .append("  link_type,").ln()
        .append("  external_id,").ln()
        .append("  link_body)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9)").ln()
        .build())
        .props(links.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getMissionId(),
                doc.getRelation() == null ? null : doc.getRelation().getObjectiveId(),
                doc.getRelation() == null ? null : doc.getRelation().getObjectiveGoalId(),
                doc.getRelation() == null ? null : doc.getRelation().getRemarkId(),
                    
                doc.getLinkType(),
                doc.getExternalId(),
                doc.getLinkBody()
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimMissionLink()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    
    .append("  mission_id VARCHAR(40) NOT NULL,").ln()
    .append("  objective_id VARCHAR(40),").ln()
    .append("  goal_id VARCHAR(40),").ln()
    .append("  remark_id VARCHAR(40),").ln()
    
    .append("  link_type VARCHAR(100) NOT NULL,").ln()
    .append("  external_id TEXT NOT NULL,").ln()
    .append("  link_body JSONB,").ln()
    
    .append("  UNIQUE NULLS NOT DISTINCT(mission_id, objective_id, goal_id, remark_id, link_type, external_id)").ln()
    
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionLink()).append("_MISSION_INDEX")
    .append(" ON ").append(options.getGrimMissionLink()).append(" (mission_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionLink()).append("_OBJECTIVE_INDEX")
    .append(" ON ").append(options.getGrimMissionLink()).append(" (objective_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionLink()).append("_GOAL_INDEX")
    .append(" ON ").append(options.getGrimMissionLink()).append(" (goal_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionLink()).append("_REMARK_INDEX")
    .append(" ON ").append(options.getGrimMissionLink()).append(" (remark_id);").ln()
    
    
    .build()).build();
  }

  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
    .ln().append("--- constraints for").append(options.getGrimMissionLink()).ln()
    
    .append("ALTER TABLE ").append(options.getGrimMissionLink()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimMissionLink()).append("_MISSION_FK").ln()
    .append("  FOREIGN KEY (mission_id)").ln()
    .append("  REFERENCES ").append(options.getGrimMission()).append(" (id);").ln().ln()
    
    .append("ALTER TABLE ").append(options.getGrimMissionLink()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimMissionLink()).append("_OBJECTIVE_FK").ln()
    .append("  FOREIGN KEY (objective_id)").ln()
    .append("  REFERENCES ").append(options.getGrimObjective()).append(" (id);").ln().ln()
    
    .append("ALTER TABLE ").append(options.getGrimMissionLink()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimMissionLink()).append("_GOAL_FK").ln()
    .append("  FOREIGN KEY (goal_id)").ln()
    .append("  REFERENCES ").append(options.getGrimObjectiveGoal()).append(" (id);").ln().ln()
    
    .append("ALTER TABLE ").append(options.getGrimMissionLink()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimMissionLink()).append("_REMARK_FK").ln()
    .append("  FOREIGN KEY (remark_id)").ln()
    .append("  REFERENCES ").append(options.getGrimRemark()).append(" (id);").ln().ln()
    
        
    .build()).build();
  }

  @Override
  public Function<Row, GrimMissionLink> defaultMapper() {
    return (row) -> {

      final var objectiveId = row.getString("objective_id");
      final var goalId = row.getString("goal_id");
      final var remarkId = row.getString("remark_id");
      
      return ImmutableGrimMissionLink.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .missionId(row.getString("mission_id"))
          .relation(GrimRegistrySqlImpl.toRelations(objectiveId, goalId, remarkId))
          
          .linkType(row.getString("link_type"))
          .externalId(row.getString("external_id"))
          .linkBody(row.getJsonObject("link_body"))
          .build();
    };
  }
  @Override
  public SqlTuple findAllByMissionIds(Collection<String> id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimMissionLink()).ln()
        .append("  WHERE (mission_id = ANY($1))").ln() 
        .build())
        .props(Tuple.of(id.toArray()))
        .build();
  }

  @Override
  public SqlTupleList deleteAll(Collection<GrimMissionLink> links) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getGrimMissionLink())
        .append(" WHERE id = $1")
        .build())
        .props(links.stream()
            .map(doc -> Tuple.from(new Object[]{doc.getId()}))
            .collect(Collectors.toList()))
        .build();
  }

}