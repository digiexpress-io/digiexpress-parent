package io.resys.thena.registry.grim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimAnyObject;
import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.entities.grim.ImmutableGrimAnyObject;
import io.resys.thena.api.entities.grim.ImmutableGrimCommitViewer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.registry.grim.GrimCommitViewerRegistry;
import io.resys.thena.api.registry.grim.GrimMissionFilter;
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
public class GrimCommitViewerRegistrySqlImpl implements GrimCommitViewerRegistry {
  private final TenantTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimCommitViewer()).append(";").ln()
        .build()).build();
  }
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getGrimCommitViewer())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimCommitViewer()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<GrimCommitViewer> commits) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getGrimCommitViewer()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()
        .append("  object_id,").ln()
        .append("  object_type,").ln()
        .append("  used_by,").ln()
        .append("  used_for,").ln()
        .append("  updated_at,").ln()
        .append("  mission_id,").ln()
        .append("  created_at)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9)").ln()
        .build())
        .props(commits.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getObjectId(),
                doc.getObjectType().name(),
                doc.getUsedBy(),
                doc.getUsedFor(),
                doc.getUpdatedAt(),
                doc.getMissionId(),
                doc.getCreatedAt()
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTupleList updateAll(Collection<GrimCommitViewer> commits) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getGrimCommitViewer()).ln()
        .append(" updated_at = $1").ln()
        .append(" WHERE id = $2").ln()
        .build())
        .props(commits.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getUpdatedAt(),
                doc.getId(), 
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimCommitViewer()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  mission_id VARCHAR(40) NOT NULL,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  object_id VARCHAR(40) NOT NULL,").ln()
    .append("  object_type VARCHAR(255) NOT NULL,").ln()
    .append("  used_by VARCHAR(255) NOT NULL,").ln()
    .append("  used_for VARCHAR(255) NOT NULL,").ln()
    .append("  updated_at TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
    .append("  created_at TIMESTAMP WITH TIME ZONE NOT NULL").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getGrimCommitViewer()).append("_MISSION_INDEX")
    .append(" ON ").append(options.getGrimCommitViewer()).append(" (mission_id);").ln()

    .append("CREATE INDEX ").append(options.getGrimCommitViewer()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getGrimCommitViewer()).append(" (commit_id);").ln()

    .append("CREATE INDEX ").append(options.getGrimCommitViewer()).append("_OBJECT_INDEX")
    .append(" ON ").append(options.getGrimCommitViewer()).append(" (object_id);").ln()

    .append("CREATE INDEX ").append(options.getGrimCommitViewer()).append("_CREATED_INDEX")
    .append(" ON ").append(options.getGrimCommitViewer()).append(" (created_at);").ln()

    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
    .ln().append("--- constraints for").append(options.getGrimCommitViewer()).ln()
    .build()).build();
  }


  @Override
  public Function<Row, GrimCommitViewer> defaultMapper() {
    return (row) -> {
      
      return ImmutableGrimCommitViewer.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .missionId(row.getString("mission_id"))

          .objectId(row.getString("object_id"))
          .objectType(GrimDocType.valueOf(row.getString("object_type")))
          .usedBy(row.getString("used_by"))
          .usedFor(row.getString("used_for"))
          .createdAt(row.getOffsetDateTime("created_at"))
          .updatedAt(row.getOffsetDateTime("updated_at"))
          
          .build();
    };
  }
  @Override
  public SqlTuple findAllByMissionIds(GrimMissionFilter filter) {
    final var where = new GrimMissionSqlFilterBuilder(options).where(filter);
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT viewers.* ").ln()
        .append("  FROM ").append(options.getGrimCommitViewer()).append(" as viewers").ln()
        //.append("  WHERE (object_id = ANY($1))").ln()
        .append("  LEFT JOIN ").append(options.getGrimMission()).append(" as mission")
        .append("  ON(viewers.object_id = mission.id)")
        .append(where.getValue()) 
        .build())
        .props(where.getProps())
        .build();
  }
  @Override
  public SqlTuple findAllObjectsByIdAndType(Collection<AnyObjectCriteria> commits) {
    final var byType = commits.stream().collect(Collectors.groupingBy(AnyObjectCriteria::getObjectType));
    final var params = new ArrayList<>();
    final var sql = new SqlStatement();
    var param = 1;
    for(final var entry : byType.entrySet()) {
      
      if(param > 1) {
        sql.ln().append("UNION ").ln();
      }
      
      switch (entry.getKey()) {
      
      case GRIM_MISSION: {
        sql
        .append("  SELECT id as object_id, commit_id, id as mission_id, '").append(entry.getKey().name()).append("' as object_type ").ln()
        .append("  FROM ").append(options.getGrimMission()).append(" ").ln()
        .append("  WHERE id = ANY($").append(param++).append(") ").ln();
        params.add(entry.getValue().stream().map(e -> e.getObjectId()).toArray());
        break;
      }
      case GRIM_MISSION_LINKS: {
        sql
        .append("  SELECT id as object_id, commit_id, mission_id, '").append(entry.getKey().name()).append("' as object_type ").ln()
        .append("  FROM ").append(options.getGrimMissionLink()).append(" ").ln()
        .append("  WHERE id = ANY($").append(param++).append(") ").ln();
        params.add(entry.getValue().stream().map(e -> e.getObjectId()).toArray());
        break;
      }
      case GRIM_MISSION_LABEL: {
        sql
        .append("  SELECT id as object_id, commit_id, mission_id, '").append(entry.getKey().name()).append("' as object_type ").ln()
        .append("  FROM ").append(options.getGrimMissionLabel()).append(" ").ln()
        .append("  WHERE id = ANY($").append(param++).append(") ").ln();
        params.add(entry.getValue().stream().map(e -> e.getObjectId()).toArray());
        break;
      }
      case GRIM_OBJECTIVE: {
        sql
        .append("  SELECT id as object_id, commit_id, mission_id, '").append(entry.getKey().name()).append("' as object_type ").ln()
        .append("  FROM ").append(options.getGrimObjective()).append(" ").ln()
        .append("  WHERE id = ANY($").append(param++).append(") ").ln();
        params.add(entry.getValue().stream().map(e -> e.getObjectId()).toArray());
        break;
      }
      case GRIM_OBJECTIVE_GOAL: {
        sql
        .append("  SELECT goal.id as object_id, goal.commit_id, objective.mission_id, '").append(entry.getKey().name()).append("' as object_type ").ln()
        .append("  FROM ").append(options.getGrimObjectiveGoal()).append(" as goal").ln()
        .append("  LEFT JOIN ").append(options.getGrimObjective()).append(" as objective").ln()
        .append("  ON(goal.objective_id = objective.id)").ln()
        .append("  WHERE id = ANY($").append(param++).append(") ").ln();
        params.add(entry.getValue().stream().map(e -> e.getObjectId()).toArray());
        break;
      }
      case GRIM_REMARK: {
        sql
        .append("  SELECT id as object_id, commit_id, mission_id, '").append(entry.getKey().name()).append("' as object_type ").ln()
        .append("  FROM ").append(options.getGrimRemark()).append(" ").ln()
        .append("  WHERE id = ANY($").append(param++).append(") ").ln();
        params.add(entry.getValue().stream().map(e -> e.getObjectId()).toArray());
        break;
      }
      case GRIM_COMMANDS: {
        sql
        .append("  SELECT id as object_id, commit_id, mission_id, '").append(entry.getKey().name()).append("' as object_type ").ln()
        .append("  FROM ").append(options.getGrimCommands()).append(" ").ln()
        .append("  WHERE id = ANY($").append(param++).append(") ").ln();
        params.add(entry.getValue().stream().map(e -> e.getObjectId()).toArray());
        break;
      }

      case GRIM_ASSIGNMENT: {
        sql
        .append("  SELECT id as object_id, commit_id, mission_id, '").append(entry.getKey().name()).append("' as object_type ").ln()
        .append("  FROM ").append(options.getGrimAssignment()).append(" ").ln()
        .append("  WHERE id = ANY($").append(param++).append(") ").ln();
        params.add(entry.getValue().stream().map(e -> e.getObjectId()).toArray());
        break;
      }
      case GRIM_MISSION_DATA: {
        sql
        .append("  SELECT id as object_id, commit_id, mission_id, '").append(entry.getKey().name()).append("' as object_type ").ln()
        .append("  FROM ").append(options.getGrimMissionData()).append(" ").ln()
        .append("  WHERE id = ANY($").append(param++).append(") ").ln();
        params.add(entry.getValue().stream().map(e -> e.getObjectId()).toArray());
        break;
      }
      case GRIM_COMMIT_VIEWER: {
        sql
        .append("  SELECT id as object_id, commit_id, mission_id, '").append(entry.getKey().name()).append("' as object_type ").ln()
        .append("  FROM ").append(options.getGrimCommitViewer()).append(" ").ln()
        .append("  WHERE id = ANY($").append(param++).append(") ").ln();
        params.add(entry.getValue().stream().map(e -> e.getObjectId()).toArray());
        break;
      }
      case GRIM_COMMIT: {
        sql
        .append("  SELECT commit_id as object_id, commit_id, mission_id, '").append(entry.getKey().name()).append("' as object_type ").ln()
        .append("  FROM ").append(options.getGrimCommit()).append(" ").ln()
        .append("  WHERE commit_id = ANY($").append(param++).append(") and mission_id is not null").append(" ").ln();
        params.add(entry.getValue().stream().map(e -> e.getObjectId()).toArray());
        break;
      }
      default: throw new IllegalArgumentException("Unexpected value: " + entry.getKey());
      } 
    }
    
    return ImmutableSqlTuple.builder()
        .value(sql.build())
        .props(Tuple.from(params))
        .build();
  }
  @Override
  public Function<Row, GrimAnyObject> anyObjectMapper() {
    return (row) -> {
      return ImmutableGrimAnyObject.builder()
          .id(row.getString("object_id"))
          .commitId(row.getString("commit_id"))
          .missionId(row.getString("mission_id"))
          .docType(GrimDocType.valueOf(row.getString("object_type")))          
          .build();
    };
  }
  @Override
  public SqlTuple findAllByUsedByAndCommit(String usedBy, String usedFor, Collection<String> commits) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimCommitViewer()).ln()
        .append("  WHERE used_by = $1 AND used_for = $2 AND commit_id = ANY($3)").ln() 
        .build())
        .props(Tuple.of(usedBy, usedFor, commits.toArray()))
        .build();
  }
  @Override
  public SqlTuple findAllByMissionIdsUsedByAndCommit(Collection<String> missionId, String usedBy, String usedFor) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimCommitViewer()).ln()
        .append("  WHERE used_by = $1 AND used_for = $2 AND mission_id = ANY($3)").ln() 
        .build())
        .props(Tuple.of(usedBy, usedFor, missionId.toArray()))
        .build();
  }
}