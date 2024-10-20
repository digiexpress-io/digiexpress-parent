package io.resys.thena.registry.grim;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimObjective;
import io.resys.thena.api.entities.grim.ImmutableGrimObjective;
import io.resys.thena.api.entities.grim.ImmutableGrimObjectiveTransitives;
import io.resys.thena.api.registry.grim.GrimMissionFilter;
import io.resys.thena.api.registry.grim.GrimObjectiveRegistry;
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
public class GrimObjectiveRegistrySqlImpl implements GrimObjectiveRegistry {
  private final TenantTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimObjective()).append(";").ln()
        .build()).build();
  }
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT objective.*,").ln()
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at,").ln()
        
        .append(" mission_data.title          as title,").ln()
        .append(" mission_data.description    as description,").ln()
        .append(" mission_data.data_extension as data_extension ").ln()
        
        .append(" FROM ").append(options.getGrimObjective()).append(" as objective ")
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = objective.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = objective.created_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimMissionData()).append(" as mission_data").ln()
        .append(" ON(mission_data.objective_id = objective.id)").ln()
        
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT objective.*,").ln()
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at,").ln()

        .append(" mission_data.title          as title,").ln()
        .append(" mission_data.description    as description,").ln()
        .append(" mission_data.data_extension as data_extension ").ln()
        
        .append(" FROM ").append(options.getGrimObjective()).append(" as objective ").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = objective.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = objective.created_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimMissionData()).append(" as mission_data").ln()
        .append(" ON(mission_data.objective_id = objective.id)").ln()
        
        .append(" WHERE objective.id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple findAllByMissionIds(GrimMissionFilter filter) {
    final var where = new GrimMissionSqlFilterBuilder(options).where(filter);
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT objective.*,").ln()
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at,").ln()

        .append(" mission_data.title          as title,").ln()
        .append(" mission_data.description    as description,").ln()
        .append(" mission_data.data_extension as data_extension ").ln()
        
        .append(" FROM ").append(options.getGrimObjective()).append(" as objective ").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = objective.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = objective.created_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimMissionData()).append(" as mission_data").ln()
        .append(" ON(mission_data.objective_id = objective.id)").ln()
        
        .append("  LEFT JOIN ").append(options.getGrimMission()).append(" as mission")
        .append("  ON(objective.mission_id = mission.id)")
        .append(where.getValue()) 
        .build())
        .props(where.getProps())
        .build();
  }
  @Override
  public SqlTupleList deleteAll(Collection<GrimObjective> objective) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getGrimObjective())
        .append(" WHERE id = $1")
        .build())
        .props(objective.stream()
            .map(doc -> Tuple.from(new Object[]{doc.getId()}))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<GrimObjective> objective) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getGrimObjective()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()
        .append("  created_commit_id,").ln()
        
        .append("  mission_id,").ln()
        .append("  objective_status,").ln()
        .append("  objective_start_date,").ln()
        .append("  objective_due_date)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7)").ln()
        .build())
        .props(objective.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getCreatedWithCommitId(),
                
                doc.getMissionId(),
                doc.getObjectiveStatus(),
                doc.getStartDate(),
                doc.getDueDate()
             }))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public SqlTupleList updateAll(Collection<GrimObjective> objective) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getGrimObjective())
        .append(" SET").ln()
        .append("  commit_id = $1,").ln()

        .append("  objective_status = $2,").ln()
        .append("  objective_start_date = $3,").ln()
        .append("  objective_due_date = $4").ln()
        .append(" WHERE id = $5")
        .build())
        .props(objective.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getCommitId(),
                doc.getObjectiveStatus(),
                doc.getStartDate(),
                doc.getDueDate(),
                
                doc.getId(), 
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimObjective()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  created_commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  mission_id VARCHAR(40) NOT NULL,").ln()
    .append("  objective_status VARCHAR(100),").ln()
    .append("  objective_start_date DATE,").ln()
    .append("  objective_due_date DATE").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getGrimObjective()).append("_CREATED_INDEX")
    .append(" ON ").append(options.getGrimObjective()).append(" (created_commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimObjective()).append("_MISSION_INDEX")
    .append(" ON ").append(options.getGrimObjective()).append(" (mission_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimObjective()).append("_STATUS_INDEX")
    .append(" ON ").append(options.getGrimObjective()).append(" (objective_status);").ln()
    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
    .ln().append("--- constraints for").append(options.getGrimObjective()).ln()
    .append("ALTER TABLE ").append(options.getGrimObjective()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimObjective()).append("_MISSION_FK").ln()
    .append("  FOREIGN KEY (mission_id)").ln()
    .append("  REFERENCES ").append(options.getGrimMission()).append(" (id);").ln().ln()
    
    .build()).build();
  }


  @Override
  public Function<Row, GrimObjective> defaultMapper() {
    return (row) -> {
      
      return ImmutableGrimObjective.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .missionId(row.getString("mission_id"))
          .createdWithCommitId(row.getString("created_commit_id"))
          
          .transitives(ImmutableGrimObjectiveTransitives.builder()
            .title(row.getString("title"))
            .description(row.getString("description"))
            .dataExtension(row.getJsonObject("data_extension"))
            .updatedAt(row.getOffsetDateTime("updated_at"))
            .createdAt(row.getOffsetDateTime("created_at"))
            .build()
          )

          .objectiveStatus(row.getString("objective_status"))
          .startDate(row.getLocalDate("objective_start_date"))
          .dueDate(row.getLocalDate("objective_due_date"))

          .build();
    };
  }
}