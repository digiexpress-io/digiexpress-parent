package io.resys.thena.registry.grim;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ImmutableGrimRemark;
import io.resys.thena.api.entities.grim.ImmutableGrimRemarkTransitives;
import io.resys.thena.api.registry.grim.GrimMissionFilter;
import io.resys.thena.api.registry.grim.GrimRemarkRegistry;
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
public class GrimRemarkRegistrySqlImpl implements GrimRemarkRegistry {
  private final TenantTableNames options;
    
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimRemark()).append(";").ln()
        .build()).build();
  }

  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT remark.*,").ln()
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at").ln()

        .append(" FROM ").append(options.getGrimRemark()).append(" as remark ")
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = remark.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = remark.created_commit_id)").ln()

        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT remark.*,").ln()
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at").ln()

        .append(" FROM ").append(options.getGrimRemark()).append(" as remark ")
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = remark.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = remark.created_commit_id)").ln()

        .append(" WHERE remark.id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple findAllByMissionIds(GrimMissionFilter filter) {
    final var where = new GrimMissionSqlFilterBuilder(options).where(filter);
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT remark.*,").ln()
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at").ln()

        .append(" FROM ").append(options.getGrimRemark()).append(" as remark ").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = remark.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = remark.created_commit_id)").ln()

        .append("  LEFT JOIN ").append(options.getGrimMission()).append(" as mission")
        .append("  ON(remark.mission_id = mission.id)").ln()
        .append(where.getValue()) 
        .build())
        .props(where.getProps())
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<GrimRemark> remarks) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getGrimRemark()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()
        .append("  created_commit_id,").ln()
        .append("  parent_id,").ln()
        
        .append("  mission_id,").ln()
        .append("  objective_id,").ln()
        .append("  goal_id,").ln()
        .append("  remark_id,").ln()
        
        .append("  reporter_id,").ln()
        .append("  remark_status,").ln()
        .append("  remark_text)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)").ln()
        .build())
        .props(remarks.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getCreatedWithCommitId(),
                
                doc.getParentId(),
                doc.getMissionId(),
                doc.getRelation() == null ? null : doc.getRelation().getObjectiveId(),
                doc.getRelation() == null ? null : doc.getRelation().getObjectiveGoalId(),
                doc.getRelation() == null ? null : doc.getRelation().getRemarkId(),
                    
                doc.getReporterId(),
                doc.getRemarkStatus(),
                doc.getRemarkText(),
             }))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public SqlTupleList updateAll(Collection<GrimRemark> remarks) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getGrimRemark())
        .append(" SET").ln()
        .append("  commit_id = $1,").ln()

        .append("  parent_id = $2,").ln()
        .append("  reporter_id = $3,").ln()
        .append("  remark_status = $4,").ln()
        .append("  remark_text = $5").ln()
        .append(" WHERE id = $6")
        .build())
        .props(remarks.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getCommitId(),
                doc.getParentId(),
                doc.getReporterId(),
                doc.getRemarkStatus(),
                doc.getRemarkText(),
                doc.getId(), 
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimRemark()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  parent_id VARCHAR(40),").ln()
    .append("  created_commit_id VARCHAR(40) NOT NULL,").ln()
    
    .append("  mission_id VARCHAR(40) NOT NULL,").ln()
    .append("  objective_id VARCHAR(40),").ln()
    .append("  goal_id VARCHAR(40),").ln()
    .append("  remark_id VARCHAR(40),").ln()
    
    .append("  reporter_id VARCHAR(255) NOT NULL,").ln()
    .append("  remark_status VARCHAR(100),").ln()
    .append("  remark_text TEXT NOT NULL").ln()
    
    
    .append(");").ln()
    
    .append("ALTER TABLE ").append(options.getGrimRemark()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimRemark()).append("_EXTRA_FK").ln()
    .append("  FOREIGN KEY (remark_id)").ln()
    .append("  REFERENCES ").append(options.getGrimRemark()).append(" (id);").ln().ln()

    .append("ALTER TABLE ").append(options.getGrimRemark()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimRemark()).append("_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent_id)").ln()
    .append("  REFERENCES ").append(options.getGrimRemark()).append(" (id);").ln().ln()
    
    .append("CREATE INDEX ").append(options.getGrimRemark()).append("_CREATED_INDEX")
    .append(" ON ").append(options.getGrimRemark()).append(" (created_commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimRemark()).append("_MISSION_INDEX")
    .append(" ON ").append(options.getGrimRemark()).append(" (mission_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimRemark()).append("_OBJECTIVE_INDEX")
    .append(" ON ").append(options.getGrimRemark()).append(" (objective_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimRemark()).append("_GOAL_INDEX")
    .append(" ON ").append(options.getGrimRemark()).append(" (goal_id);").ln()
    
    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
      
      .append("ALTER TABLE ").append(options.getGrimRemark()).ln()
      .append("  ADD CONSTRAINT ").append(options.getGrimRemark()).append("_MISSION_FK").ln()
      .append("  FOREIGN KEY (mission_id)").ln()
      .append("  REFERENCES ").append(options.getGrimMission()).append(" (id);").ln().ln()
      
      .append("ALTER TABLE ").append(options.getGrimRemark()).ln()
      .append("  ADD CONSTRAINT ").append(options.getGrimRemark()).append("_OBJECTIVE_FK").ln()
      .append("  FOREIGN KEY (objective_id)").ln()
      .append("  REFERENCES ").append(options.getGrimObjective()).append(" (id);").ln().ln()
      
      .append("ALTER TABLE ").append(options.getGrimRemark()).ln()
      .append("  ADD CONSTRAINT ").append(options.getGrimRemark()).append("_GOAL_FK").ln()
      .append("  FOREIGN KEY (goal_id)").ln()
      .append("  REFERENCES ").append(options.getGrimObjectiveGoal()).append(" (id);").ln().ln()
      
      .build()).build();
  }


  @Override
  public Function<Row, GrimRemark> defaultMapper() {
    return (row) -> {
      
      final var objectiveId = row.getString("objective_id");
      final var goalId = row.getString("goal_id");
      final var remarkId = row.getString("remark_id");
      
      return ImmutableGrimRemark.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .parentId(row.getString("parent_id"))
          
          
          .transitives(ImmutableGrimRemarkTransitives.builder()
            .updatedAt(row.getOffsetDateTime("updated_at"))
            .createdAt(row.getOffsetDateTime("created_at"))
            .build()
          )
        
          .createdWithCommitId(row.getString("created_commit_id"))
          
          .missionId(row.getString("mission_id"))
          .remarkText(row.getString("remark_text"))
          .remarkStatus(row.getString("remark_status"))
          .reporterId(row.getString("reporter_id"))
          
          .relation(GrimRegistrySqlImpl.toRelations(objectiveId, goalId, remarkId))
          .build();
    };
  }

  @Override
  public SqlTupleList deleteAll(Collection<GrimRemark> remarks) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getGrimRemark())
        .append(" WHERE id = $1")
        .build())
        .props(remarks.stream()
            .map(doc -> Tuple.from(new Object[]{doc.getId()}))
            .collect(Collectors.toList()))
        .build();
  }

}