package io.digiexpress.mig.client.spi;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.digiexpress.mig.client.api.ImmutableSourceAccess;
import io.digiexpress.mig.client.api.ImmutableSourceComment;
import io.digiexpress.mig.client.api.ImmutableSourceKeywords;
import io.digiexpress.mig.client.api.ImmutableSourceLink;
import io.digiexpress.mig.client.api.ImmutableSourceProcess;
import io.digiexpress.mig.client.api.ImmutableSourceRole;
import io.digiexpress.mig.client.api.ImmutableSourceTask;
import io.digiexpress.mig.client.api.ImmutableSourceTasks;
import io.digiexpress.mig.client.api.ImmutableSourceWorkflow;
import io.digiexpress.mig.client.api.MigClient.SourceTaskQuery;
import io.digiexpress.mig.client.api.SourceTasks;
import io.digiexpress.mig.client.api.SourceTasks.SourceAccess;
import io.digiexpress.mig.client.api.SourceTasks.SourceComment;
import io.digiexpress.mig.client.api.SourceTasks.SourceKeywords;
import io.digiexpress.mig.client.api.SourceTasks.SourceLink;
import io.digiexpress.mig.client.api.SourceTasks.SourceProcess;
import io.digiexpress.mig.client.api.SourceTasks.SourceRole;
import io.digiexpress.mig.client.api.SourceTasks.SourceTask;
import io.digiexpress.mig.client.api.SourceTasks.SourceWorkflow;
import io.digiexpress.mig.client.spi.loggers.SourceTasksLogger;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class SourceTaskQueryImpl implements SourceTaskQuery {
  private final SourceTasksLogger logger = new SourceTasksLogger();
  private final io.vertx.mutiny.pgclient.PgPool pool;
  
  @Override
  public Uni<SourceTasks> findAll() {
    return Uni.combine().all().unis(
        getTasks(),
        getRoles(),
        getKeywords(),
        getComments(),
        getLinks(),
        getAccess(),
        getProcesses(),
        getWorkflows()
    ).asTuple().onItem().transform(sources -> {
      final SourceTasks result = ImmutableSourceTasks.builder()
          .tasks(sources.getItem1().stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .roles(sources.getItem2().stream().collect(Collectors.groupingBy(e -> e.getTask_id())))
          .keywords(sources.getItem3().stream().collect(Collectors.groupingBy(e -> e.getTask_id())))
          .comments(sources.getItem4().stream().collect(Collectors.groupingBy(e -> e.getTask_id())))
          .links(sources.getItem5().stream().collect(Collectors.groupingBy(e -> e.getTask_id())))
          .access(sources.getItem6().stream().collect(Collectors.groupingBy(e -> e.getTask_id())))
          .processes(sources.getItem7().stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .workflows(sources.getItem8().stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build();
      logger.ok(result);
      return result;
    })
    .onFailure().invoke(e -> logger.fail(e));
  }
  
  private Uni<List<SourceProcess>> getProcesses() {
    final var sql = "select * from process order by id";
    return processAnyQuery(SourceProcess.class, sql, (row) -> ImmutableSourceProcess.builder()
        .id(row.getLong("id"))
        .workflow_name(row.getString("workflow_name"))
        .updated(row.getLocalDateTime("updated"))
        .created(row.getLocalDateTime("created"))

        .status(Optional.ofNullable(row.getString("status")))
        .questionnaire_id(Optional.ofNullable(row.getString("questionnaire_id")))
        .task_id(Optional.ofNullable(row.getString("task_id")).map(e -> Long.parseLong(e)))
        .user_id(Optional.ofNullable(row.getString("user_id")))
        .input_context_id(Optional.ofNullable(row.getString("input_context_id")))
        .input_parent_context_id(Optional.ofNullable(row.getString("input_parent_context_id")))
        
        .build());
  }
  
  private Uni<List<SourceWorkflow>> getWorkflows() {
    final var sql = "select * from workflow order by id";
    return processAnyQuery(SourceWorkflow.class, sql, (row) -> ImmutableSourceWorkflow.builder()
        .id(row.getLong("id"))
        .name(row.getString("name"))
        .updated(row.getLocalDateTime("updated"))
        .form_name(row.getString("form_name"))
        .form_tag(row.getString("form_tag"))
        .end_date(Optional.ofNullable(row.getLocalDate("end_date")))
        .flow_name(Optional.ofNullable(row.getString("flow_name")))
        .form_id(Optional.ofNullable(row.getString("form_id")))
        .start_date(Optional.ofNullable(row.getLocalDate("start_date")))
        .build());
  }
  
  private Uni<List<SourceAccess>> getAccess() {
    final var sql = "select * from task_access order by task_id";
    return processAnyQuery(SourceAccess.class, sql, (row) -> ImmutableSourceAccess.builder()
        .task_id(row.getLong("task_id"))
        .updated(row.getLocalDateTime("updated"))
        .user_id(row.getString("user_id"))
        .build());
  }
  
  private Uni<List<SourceLink>> getLinks() {
    final var sql = "select * from task_link order by task_id";
    return processAnyQuery(SourceLink.class, sql, (row) -> ImmutableSourceLink.builder()
        .id(row.getLong("id"))
        .task_id(row.getLong("task_id"))
        .link_key(row.getString("link_key"))
        .link_address(row.getString("link_address"))
        .build());
  }
  
  private Uni<List<SourceComment>> getComments() {
    final var sql = "select * from comment order by task_id";
    return processAnyQuery(SourceComment.class, sql, (row) -> ImmutableSourceComment.builder()
        .task_id(row.getLong("task_id"))
        .id(row.getLong("id"))
        .comment_text(row.getString("comment_text"))
        .created(row.getLocalDateTime("created"))
        .user_name(row.getString("user_name"))
        .reply_to_id(Optional.ofNullable(row.getLong("reply_to_id")))
        .external(Optional.ofNullable(row.getBoolean("external")))
        .source(Optional.ofNullable(row.getString("source")))
        .build());
  }
  
  
  private Uni<List<SourceKeywords>> getKeywords() {
    final var sql = "select * from task_keywords order by task_id";
    return processAnyQuery(SourceKeywords.class, sql, (row) -> ImmutableSourceKeywords.builder()
        .task_id(row.getLong("task_id"))
        .key_words(row.getString("key_words"))
        .build());
  }
  
  private Uni<List<SourceRole>> getRoles() {
    final var sql = "select * from task_roles order by task_id";
    return processAnyQuery(SourceRole.class, sql, row -> ImmutableSourceRole.builder()
        .task_id(row.getLong("task_id"))
        .assigned_roles(row.getString("assigned_roles"))
        .build());
  }
  

  private Uni<List<SourceTask>> getTasks() {
    final var sql = "select * from task order by id";
    return processAnyQuery(SourceTask.class, sql, (row) ->  ImmutableSourceTask.builder()
      .id(row.getLong("id"))
      .created(row.getLocalDateTime("created"))
      .priority(row.getInteger("priority"))
      .status(row.getInteger("status"))
      .updated(row.getLocalDateTime("updated"))
      .version(row.getInteger("version"))
      
      .completed(Optional.ofNullable(row.getLocalDateTime("completed")))
      .description(Optional.ofNullable(row.getString("description")))
      .due_date(Optional.ofNullable(row.getLocalDate("due_date")))
      .subject(Optional.ofNullable(row.getString("subject")))
      .updater_id(Optional.ofNullable(row.getString("updater_id")))
      .assigned_user(Optional.ofNullable(row.getString("assigned_user")))
      .client_identificator(Optional.ofNullable(row.getString("client_identificator")))
      .assigned_user_email(Optional.ofNullable(row.getString("assigned_user_email")))
      .task_ref(Optional.ofNullable(row.getString("task_ref")))
      .build());
  }
  
  

  private <T> Uni<List<T>> processAnyQuery(Class<T> type, String sql, Function<io.vertx.mutiny.sqlclient.Row, T> mapper) {
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
}
