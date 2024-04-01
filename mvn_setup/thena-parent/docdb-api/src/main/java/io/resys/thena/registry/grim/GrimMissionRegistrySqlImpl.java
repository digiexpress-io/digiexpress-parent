package io.resys.thena.registry.grim;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.ImmutableGrimMission;
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
        .append("SELECT * FROM ").append(options.getGrimMission())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimMission()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
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
        .append("  archived_status)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)").ln()
        .build())
        .props(mission.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getParentMissionId(),
                doc.getReporterId(),
                doc.getExternalId(),
                doc.getMissionStatus(),
                doc.getMissionPriority(),
                doc.getStartDate(),
                doc.getDueDate(),
                
                doc.getArchivedDate(),
                doc.getArchivedStatus()
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
        .append("  archived_status = $10").ln()
        
        .append(" WHERE id = $9")
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
    .append("  parent_mission_id VARCHAR(40),").ln()
    .append("  external_id VARCHAR(40) UNIQUE,").ln()
    .append("  reporter_id VARCHAR(255),").ln()
    
    .append("  mission_status VARCHAR(100),").ln()
    .append("  mission_priority VARCHAR(100),").ln()
    .append("  mission_start_date TIMESTAMP,").ln()
    .append("  mission_due_date TIMESTAMP").ln()
    
    .append(");").ln()
    
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

    .append("CREATE INDEX ").append(options.getGrimMission()).append("_PARENT_INDEX")
    .append(" ON ").append(options.getGrimMission()).append(" (parent_mission_id);").ln()
    
    .build()).build();
  }


  @Override
  public Function<Row, GrimMission> defaultMapper() {
    return (row) -> {
      
      return ImmutableGrimMission.builder()
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
        .append("  FROM ").append(options.getGrimMission()).ln()
        .append("  WHERE (mission_id = ANY($1))").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }

}