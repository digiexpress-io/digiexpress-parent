package io.digiexpress.mig.client.spi;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.eveli.client.spi.task.TaskMapper;
import io.digiexpress.mig.client.api.MigClient.TargetTaskBuilder;
import io.digiexpress.mig.client.api.SourceTasks;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger;
import io.digiexpress.mig.client.spi.loggers.TargetTaskLogger;
import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.GrimCommit;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.GrimMissionLabel;
import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.datasource.TenantTableNames;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class TargetTaskBuilderImpl implements TargetTaskBuilder {
  private final TargetTaskLogger logger = new TargetTaskLogger();
  private final io.vertx.mutiny.pgclient.PgPool target_tasks;
  private final TenantTableNames names; 
  

/**
delete from task_tenan13_grim_commands;
delete from task_tenan13_grim_mission_link;
delete from task_tenan13_grim_mission_label;
delete from task_tenan13_grim_mission_data;
delete from task_tenan13_grim_commit_viewer;
delete from task_tenan13_grim_commit_tree;
delete from task_tenan13_grim_assignment;
delete from task_tenan13_grim_remark ;
delete from task_tenan13_grim_mission;
delete from task_tenan13_grim_commit;
*/
  

  enum TaskStatus { NEW, OPEN, COMPLETED, REJECTED, DELEGATED }
  enum TaskPriority { LOW, NORMAL, HIGH }
  
  public TargetTaskBuilderImpl(PgPool target_tasks, String tenantName) {
    super();
    this.target_tasks = target_tasks;
    this.names = TenantTableNames.defaults("").toRepo(tenantName);
  }
  
  
  @Override
  public Uni<SourceTasks> build(SourceTasks source) {
    return target_tasks.withTransaction(conn -> execute(conn, source));
  }
  
  private Uni<SourceTasks> execute(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceTasks source) {
    
    return Uni.combine().all().unis(
        createCommits(conn, source),
        createTasks(conn, source),
        createAssignees(conn, source),
        createRoles(conn, source),
        createComments(conn, source),
        createKeywords(conn, source),
        createAccess(conn, source)
    ).asTuple().onItem().transform(e -> {
      logger.ok(source);
      
      return source;
    });
  }
  

  
  
  private Uni<?> createTasks(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceTasks source) {
    final var sql = "INSERT INTO  " + names.getGrimMission() +
"""
 (id,
  commit_id,

  parent_mission_id,
  external_id,
  reporter_id,

  mission_status,
  mission_priority,
  mission_start_date,
  mission_due_date,

  mission_title,
  mission_description,
  mission_completed_at,

  archived_at,
  archived_status,
  mission_ref,
  questionnaire_id,

  created_commit_id,
  updated_tree_commit_id)

 VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17, $18)
  
 ON CONFLICT (id) DO NOTHING
 RETURNING *
""";
    
    
    final var props = source.getTasks().values().stream()
        .map(doc -> {
          
          final var questionnaire_id = source.getQuestionnaireId(doc.getId());
          final var commits = TargetTaskCommit.of(doc, source);
          
          return Tuple.from(Arrays.asList(
              String.valueOf(doc.getId()),
              commits.updatedWithCommit(),
              
              null, //parent_mission_id
              null, //external_id
              doc.getClient_identificator().orElse(null),
              TaskStatus.values()[doc.getStatus()],
              TaskStatus.values()[doc.getPriority()],
              null,
              doc.getDue_date().orElse(null),
              doc.getSubject().orElse(null),
              doc.getDescription().orElse(null),
              doc.getCompleted().map(e -> e.atZone(source.getZoneId()).toOffsetDateTime()).orElse(null),
              null, //archived_at
              null, //archived_status
              doc.getTask_ref().orElse(null),
              questionnaire_id,
              
              commits.createdWithCommit(),
              commits.treeUpdatedWithCommit()
            ));
        })
        .collect(Collectors.toList());
     
      return batch(conn, GrimMission.class, sql, props);
  }
  
  
  
  
  private Uni<?> createAssignees(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceTasks source) {
    final var sql = "INSERT INTO  " + names.getGrimAssignment() +
"""
 (id,
  commit_id,
  mission_id,
  objective_id,
  goal_id,
  remark_id, 
  assignee, 
  assignment_type,
  assignee_contact)

 VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9)
 ON CONFLICT (id) DO NOTHING
 RETURNING *
""";
    final var props = source.getTasks().values().stream()
        .filter(doc -> doc.getAssigned_user().isPresent())
        .filter(doc -> !doc.getAssigned_user().get().isBlank())
        .map(doc -> {
          final var commits = TargetTaskCommit.of(doc, source);
          return Tuple.from(Arrays.asList(
              doc.getAssigneeGid(),
              commits.createdWithCommit(),
              String.valueOf(doc.getId()),
              null, //objective_id
              null, //goal_id
              null, //remark_id
              doc.getAssigned_user().get(),
              TaskMapper.ASSIGNMENT_TYPE_TASK_USER,
              doc.getAssigned_user_email().orElse(null)
            ));
        })
        .collect(Collectors.toList());
     
      return batch(conn, GrimAssignment.class, sql, props);
  }
  private Uni<?> createRoles(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceTasks source) {

    final var sql = "INSERT INTO  " + names.getGrimAssignment() +
"""
 (id,
  commit_id,
  mission_id,
  objective_id,
  goal_id,
  remark_id, 
  assignee, 
  assignment_type,
  assignee_contact)

 VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9)
 ON CONFLICT (id) DO NOTHING
 RETURNING *
""";
    final var props = source.getRoles().values().stream()
        .flatMap(e -> e.stream())
        .map(doc -> {
          final var commits = TargetTaskCommit.of(doc.getTask_id(), source);
          final var task = source.getTasks().get(doc.getTask_id());
          return Tuple.from(Arrays.asList(
              doc.getGid(),
              commits.createdWithCommit(),
              String.valueOf(task.getId()),
              null, //objective_id
              null, //goal_id
              null, //remark_id
              doc.getAssigned_roles(),
              TaskMapper.ASSIGNMENT_TYPE_TASK_ROLE,
              null
            ));
        })
        .collect(Collectors.toList());
     
      return batch(conn, GrimAssignment.class, sql, props);
  }

  private Uni<?> createCommits(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceTasks source) {
    
    final var sql = "INSERT INTO  " + names.getGrimCommit() +        
        """
 (commit_id,
  parent_id,
  mission_id,
  created_at,
  commit_log, 
  commit_author, 
  commit_message)
  VALUES($1, $2, $3, $4, $5, $6, $7)
  ON CONFLICT (commit_id) DO NOTHING
  RETURNING commit_id
""";
    final var props = source.getTasks().values().stream()
        .flatMap(doc -> {
          return TargetTaskCommit.of(doc, source).getEvents().stream()
          .map(event -> Tuple.from(Arrays.asList(
              event.getEventId(),
              event.getParentEventId(),
              String.valueOf(doc.getId()),
              event.getEventDate(),
              "conversion commit for comment at date",
              event.getEventAuthor(),
              "created with conversion at: " + OffsetDateTime.now()
            )));
        })
        .collect(Collectors.toList());
      return batch(conn, GrimCommit.class, sql, props);
  }
  
  
 private Uni<?> createComments(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceTasks source) {
    final var sql = "INSERT INTO  " + names.getGrimRemark() +        
"""
  (id,
  commit_id,
  created_commit_id,
  parent_id,
 
  mission_id,
  objective_id,
  goal_id,
  remark_id,
 
  reporter_id,
  remark_status,
  remark_type,
  remark_source,
  remark_text)
 
  VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)
  ON CONFLICT (id) DO NOTHING
  RETURNING id
""";
      final var props = source.getComments().values().stream()
      .flatMap(e -> e.stream())
      .sorted((a, b) ->  Long.compare(
          a.getReply_to_id().orElse((long) -1), 
          b.getReply_to_id().orElse((long) -1)
      ))
      .map(doc -> {
        
        final String parentId = doc.getReployToGid().orElse(null);
        final var remarkType = doc.getRemarkType();
        final String commitId = TargetTaskCommit.createCommentEvent(doc, source).getEventId();
        
        return Tuple.from(Arrays.asList(
            doc.getGid(),
            commitId, // commit_id
            commitId, // created_commit_id
            parentId, // parent_id
            String.valueOf(doc.getTask_id()),
            null, //objective_id
            null, //goal_id
            null, //remark_id
            
            doc.getUser_name(),
            null, //remark_status
            remarkType, //remark_type
            doc.getSource().orElse(null),
            doc.getComment_text()
          ));
      })
      .collect(Collectors.toList());
   
    return batch(conn, GrimRemark.class, sql, props);
  }
  
 private Uni<?> createKeywords(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceTasks source) {
    final var sql = "INSERT INTO  " + names.getGrimMissionLabel() +        
"""
 (id,
  commit_id,

  mission_id,
  objective_id,
  goal_id,
  remark_id,

  label_type,
  label_value,
  label_body)

  VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9)
  ON CONFLICT (id) DO NOTHING
  RETURNING id
""";
    final var props = source.getKeywords().values().stream()
        .flatMap(e -> e.stream())
        .map(doc -> {
          final var task = source.getTasks().get(doc.getTask_id());
          final String commitId = TargetTaskCommit.of(task, source).createdWithCommit();
          
          return Tuple.from(Arrays.asList(
              doc.getGid(),
              commitId,
              String.valueOf(doc.getTask_id()),
              null, //objective_id
              null, //goal_id
              null, //remark_id
              TaskMapper.LABEL_TYPE_KEYWORD,
              doc.getKey_words(),
              null //label_body
            ));
        })
        .collect(Collectors.toList());
     
      return batch(conn, GrimMissionLabel.class, sql, props);
  }
  
 

 private Uni<?> createAccess(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceTasks source) {
    final var sql = "INSERT INTO  " + names.getGrimCommitViewer() +        
"""
  (id,
  commit_id,
  object_id,
  object_type,
  used_by,
  used_for,
  updated_at,
  mission_id,
  created_at)

  VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9)
  ON CONFLICT (id) DO NOTHING
  RETURNING id
""";
    final var props = source.getAccess().values().stream()
        .flatMap(e -> e.stream())
        .map(doc -> {
          final var task = source.getTasks().get(doc.getTask_id());
          final var commitId = TargetTaskCommit.of(task, source).treeUpdatedWithCommit();
          
          return Tuple.from(Arrays.asList(
              doc.getGid(),
              commitId,
              String.valueOf(task.getId()), //object_id
              GrimDocType.GRIM_MISSION.name(), //object_type
              doc.getUser_id(), //used_by
              "conversion", //used_for
              doc.getUpdated().atZone(source.getZoneId()).toOffsetDateTime(), //updated_at
              String.valueOf(doc.getTask_id()), 
              doc.getUpdated().atZone(source.getZoneId()).toOffsetDateTime()
            ));
        })
        .collect(Collectors.toList());
     
      return batch(conn, GrimMissionLabel.class, sql, props);
  }
 

  private <T> Uni<RowSet<Row>> batch(
      io.vertx.mutiny.sqlclient.SqlConnection conn,
      Class<T> type,
      String sql, 
      List<Tuple> props
  ) {
    
    final EntityQueryLogger<T> logger = this.logger.entityQuery(type).query(sql, props);
    return conn.preparedQuery(sql).executeBatch(props)
      .onItem().invoke(data -> logger.queryOk(data))
      .onFailure().invoke(e -> logger.queryFail(e));
  }

}
