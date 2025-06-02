package io.digiexpress.mig.client.spi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.digiexpress.eveli.client.spi.task.TaskMapper;
import io.digiexpress.mig.client.api.MigClient.TargetTaskRolesBuilder;
import io.digiexpress.mig.client.api.SourceTasks;
import io.digiexpress.mig.client.api.SourceTasks.SourceRole;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger;
import io.digiexpress.mig.client.spi.loggers.TargetTaskLogger;
import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.datasource.TenantContext;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TargetTaskRolesBuilderImpl implements TargetTaskRolesBuilder {
  private final TargetTaskLogger logger = new TargetTaskLogger();
  private final io.vertx.mutiny.pgclient.PgPool target_tasks;
  private TenantContext names;  
  private final AtomicLong counter = new AtomicLong();
  
  @Override
  public Uni<SourceTasks> build(SourceTasks source, String tenantName) {
    this.names = TenantContext.defaults("").withTenantPrefix(tenantName);
    return target_tasks.withTransaction(conn -> execute(conn, source));
  }
  
  private Uni<SourceTasks> execute(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceTasks source) {
    
    return createRoles(conn, source).onItem().transform(e -> {
      logger.ok(source);
      
      return source;
    });
  }

  @Data
  @Builder
  public static class SyncTaskRole {
    private final String id;
    private final String missionId;
    private final String assignee;
  }
  
  private Uni<List<SyncTaskRole>> getSyncRoles() {
    final var sql =
    """
SELECT 
  id,
  mission_id,
  assignee
FROM
    """ + names.getGrimAssignment() +
" WHERE assignment_type = 'task_role'";
    
  return processAnyQuery(target_tasks, SyncTaskRole.class, sql, (row) -> SyncTaskRole.builder()
      .id(row.getString("id"))
      .missionId(row.getString("mission_id"))
      .assignee(row.getString("assignee"))      
      .build());
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
    
    
    return getSyncRoles().onItem().transform(existingRoles -> existingRoles.stream().collect(Collectors.groupingBy(e -> e.getMissionId())))
    .onItem().transformToUni(existingData -> {
      
      
      final var props = source.getRoles().values().stream()
          .flatMap(e -> e.stream())
          .map(doc -> {
            
            final var existingTaskRoles = Optional.ofNullable(existingData.get(String.valueOf(doc.getTask_id()))).orElse(Collections.emptyList());
            final var found = existingTaskRoles.stream().filter(e -> doc.getAssigned_roles().equals(e.getAssignee())).findAny();
            
            if(found.isPresent()) {
              logger.skipRole(doc);
              return Optional.<Tuple>empty();
            }
            
            final var sequence = allocateTaskSeq(source, doc.getTask_id(), existingTaskRoles, doc);
            
            final var commits = TargetTaskCommit.of(doc.getTask_id(), source);
            final var task = source.getTasks().get(doc.getTask_id());
            final var insert = Tuple.from(Arrays.asList(
                doc.getGid() + "_" + sequence,
                commits.createdWithCommit(),
                String.valueOf(task.getId()),
                null, //objective_id
                null, //goal_id
                null, //remark_id
                doc.getAssigned_roles(),
                TaskMapper.ASSIGNMENT_TYPE_TASK_ROLE,
                null
              ));
            
            
            return Optional.of(insert);
          })
          .filter(tuple -> tuple.isPresent())
          .map(tuple -> tuple.get())
          .collect(Collectors.toList());
      
      
      return batch(conn, GrimAssignment.class, sql, props);
    });
  }
  
  private int allocateTaskSeq(SourceTasks source, long taskId, List<SyncTaskRole> existingTaskRoles, SourceRole role) {
    final var toBeMapped = Optional.ofNullable(source.getRoles().get(taskId))
        .orElse(Collections.emptyList())
        .stream()
        .map(e -> e.getAssigned_roles())
        .sorted((a, b) -> a.compareTo(b))
        .toList();
    final var index = toBeMapped.indexOf(role.getAssigned_roles());
    return index;
  }
  
  private <T> Uni<List<T>> processAnyQuery(
      io.vertx.mutiny.pgclient.PgPool pool, 
      Class<T> type, 
      String sql, 
      Function<io.vertx.mutiny.sqlclient.Row, T> mapper) {
    final var logger = this.logger.entityQuery(type);
    logger.query(sql);
    return pool.preparedQuery(sql)
      .mapping(row -> {
        try {
          final T result = mapper.apply(row);
          logger.mappingOk(result);
          return result;
        } catch(Exception e) {
          logger.mappingFail(row, e);
          return null;
        }
      })
      .execute()
      .onItem()
      .transformToMulti(RowSet::toMulti).collect().asList()
      .onItem().transform(data -> data.stream().filter(e -> e != null).toList())
      .onItem().invoke(e -> logger.queryOk(e))
      .onFailure().invoke(e -> logger.queryFail(e));
  }
  
  private <T> Uni<RowSet<Row>> batch(
      io.vertx.mutiny.sqlclient.SqlConnection conn,
      Class<T> type,
      String sql, 
      List<Tuple> props
  ) {
    
    if(props.isEmpty()) {
      return Uni.createFrom().item(new RowSet(null));
    }
    
    
    final EntityQueryLogger<T> logger = this.logger.entityQuery(type).query(sql, props);
    return conn.preparedQuery(sql).executeBatch(props)
      .onItem().invoke(data -> logger.queryOk(data))
      .onFailure().invoke(e -> logger.queryFail(e));
  }

}
