package io.resys.thena.registry.grim;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.ImmutableGrimMission;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionTransitives;
import io.resys.thena.api.registry.grim.GrimMissionRegistry;
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
public class GrimMissionRegistrySqlImpl implements GrimMissionRegistry {
  private final TenantTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimMission()).append(";").ln()
        .build()).build();
  }
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append(" mission.*, ")
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at,").ln()
        .append(" updated_tree_commit.created_at  as tree_updated_at,").ln()
        
        .append(" mission_data.title          as title,").ln()
        .append(" mission_data.description    as description,").ln()
        .append(" mission_data.data_extension as data_extension ").ln()
        
        .append(" FROM ").append(options.getGrimMission()).append(" as mission ").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = mission.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = mission.created_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_tree_commit").ln()
        .append(" ON(updated_tree_commit.commit_id = mission.updated_tree_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimMissionData()).append(" as mission_data").ln()
        .append(" ON(mission_data.mission_id = mission.id and objective_id is null and goal_id is null and remark_id is null)").ln()
        
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append(" mission.*, ")
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at,").ln()
        .append(" updated_tree_commit.created_at  as tree_updated_at, ").ln()
        
        .append(" mission_data.title          as title,").ln()
        .append(" mission_data.description    as description,").ln()
        .append(" mission_data.data_extension as data_extension ").ln()
        
        .append(" FROM ").append(options.getGrimMission()).append(" as mission ").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = mission.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = mission.created_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_tree_commit").ln()
        .append(" ON(updated_tree_commit.commit_id = mission.updated_tree_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimMissionData()).append(" as mission_data").ln()
        .append(" ON(mission_data.mission_id = mission.id and objective_id is null and goal_id is null and remark_id is null)").ln()
        
        .append(" WHERE mission.id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple findAllByMissionIds(Collection<String> id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append(" mission.*, ")
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at,").ln()
        .append(" updated_tree_commit.created_at  as tree_updated_at,").ln()
        
        .append(" mission_data.title          as title,").ln()
        .append(" mission_data.description    as description,").ln()
        .append(" mission_data.data_extension as data_extension ").ln()
        
        .append(" FROM ").append(options.getGrimMission()).append(" as mission ").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = mission.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = mission.created_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_tree_commit").ln()
        .append(" ON(updated_tree_commit.commit_id = mission.updated_tree_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimMissionData()).append(" as mission_data").ln()
        .append(" ON(mission_data.mission_id = mission.id and objective_id is null and goal_id is null and remark_id is null)").ln()
        
        .append(" WHERE mission.id = ANY($1)").ln() 
        .build())
        .props(Tuple.of(id.toArray()))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<GrimMission> mission) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getGrimMission()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()

        .append("  parent_mission_id,").ln()
        .append("  external_id,").ln()
        .append("  reporter_id,").ln()
        
        .append("  mission_status,").ln()
        .append("  mission_priority,").ln()
        .append("  mission_start_date,").ln()        
        .append("  mission_due_date,").ln()
        
        .append("  archived_date,").ln()
        .append("  archived_status,").ln()
        
        .append("  created_commit_id,").ln()
        .append("  updated_tree_commit_id)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)").ln()
        .build())
        .props(mission.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getParentMissionId(),
                doc.getExternalId(),
                doc.getReporterId(),
                
                doc.getMissionStatus(),
                doc.getMissionPriority(),
                doc.getStartDate(),
                doc.getDueDate(),
                
                doc.getArchivedDate(),
                doc.getArchivedStatus(),
                
                doc.getCreatedWithCommitId(),
                doc.getUpdatedTreeWithCommitId()
                
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTupleList updateAll(Collection<GrimMission> mission) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getGrimMission())
        .append(" SET").ln()
        .append("  commit_id = $1,").ln()
        .append("  parent_mission_id = $2,").ln()
        .append("  external_id = $3,").ln()
        .append("  reporter_id = $4,").ln()
        
        .append("  mission_status = $5,").ln()
        .append("  mission_priority = $6,").ln()
        .append("  mission_start_date = $7,").ln()        
        .append("  mission_due_date = $8,").ln()
        .append("  archived_date = $9,").ln()
        .append("  archived_status = $10,").ln()
        
        .append("  created_commit_id = $11,").ln()
        .append("  updated_tree_commit_id = $12").ln()
        
        .append(" WHERE id = $13")
        .build())
        .props(mission.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getCommitId(),
                doc.getParentMissionId(),
                doc.getReporterId(),
                doc.getExternalId(),
                doc.getMissionStatus(),
                doc.getMissionPriority(),
                doc.getStartDate(),
                doc.getDueDate(),
                
                doc.getArchivedDate(),
                doc.getArchivedStatus(),
                
                doc.getCreatedWithCommitId(),
                doc.getUpdatedTreeWithCommitId(),
                
                doc.getId(), 
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimMission()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  created_commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  updated_tree_commit_id VARCHAR(40) NOT NULL,").ln()
    
    .append("  parent_mission_id VARCHAR(40),").ln()
    .append("  external_id VARCHAR(40) UNIQUE,").ln()
    .append("  reporter_id VARCHAR(255),").ln()
    
    .append("  mission_status VARCHAR(100),").ln()
    .append("  mission_priority VARCHAR(100),").ln()
    .append("  mission_start_date DATE,").ln()
    .append("  mission_due_date DATE,").ln()
    .append("  archived_date DATE,").ln()
    .append("  archived_status VARCHAR(40)").ln()
    
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMission()).append("_PARENT_INDEX")
    .append(" ON ").append(options.getGrimMission()).append(" (parent_mission_id);").ln()

    .append("CREATE INDEX ").append(options.getGrimMission()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getGrimMission()).append(" (commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMission()).append("_CREATED_INDEX")
    .append(" ON ").append(options.getGrimMission()).append(" (created_commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMission()).append("_TREE_UPDATED_INDEX")
    .append(" ON ").append(options.getGrimMission()).append(" (updated_tree_commit_id);").ln()
    
    .append("ALTER TABLE ").append(options.getGrimMission()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimMission()).append("_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent_mission_id)").ln()
    .append("  REFERENCES ").append(options.getGrimMission()).append(" (id);").ln()
    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
    .ln().append("--- constraints for").append(options.getGrimMission()).ln()

    
    .build()).build();
  }


  @Override
  public Function<Row, GrimMission> defaultMapper() {
    return (row) -> {
      
      return ImmutableGrimMission.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .createdWithCommitId(row.getString("created_commit_id"))
          .updatedTreeWithCommitId(row.getString("updated_tree_commit_id"))

          .transitives(ImmutableGrimMissionTransitives.builder()
            .title(row.getString("title"))
            .description(row.getString("description"))
            .dataExtension(row.getJsonObject("data_extension"))
            .updatedAt(row.getOffsetDateTime("updated_at"))
            .createdAt(row.getOffsetDateTime("created_at"))
            .treeUpdatedAt(row.getOffsetDateTime("tree_updated_at"))
            .build()
          )
          
          .parentMissionId(row.getString("parent_mission_id"))
          .externalId(row.getString("external_id"))
          .reporterId(row.getString("reporter_id"))
          
          .missionStatus(row.getString("mission_status"))
          .missionPriority(row.getString("mission_priority"))
          .startDate(row.getLocalDate("mission_start_date"))        
          .dueDate(row.getLocalDate("mission_due_date"))
          
          .archivedDate(row.getLocalDate("archived_date"))
          .archivedStatus(row.getString("archived_status"))
          .build();
    };
  }


}