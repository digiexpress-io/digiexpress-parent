package io.digiexpress.mig.client.spi.loggers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.digiexpress.eveli.client.persistence.entities.ProcessEntity;
import io.digiexpress.mig.client.api.ImmutableSourceRole;
import io.digiexpress.mig.client.api.SourceTasks;
import io.digiexpress.mig.client.api.SourceTasks.SourceRole;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger.EntityQueryLoggerImpl;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger.LogEvent;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger.LogEventLevel;
import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.GrimCommit;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.GrimMissionLabel;
import io.resys.thena.api.entities.grim.GrimRemark;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TargetTaskLogger {
  private final List<LogEvent> messages = Collections.synchronizedList(new ArrayList<>());
  private final Map<Class<?>, Integer> inserted_count = Collections.synchronizedMap(new HashMap<>());
  
  
  public <T> EntityQueryLogger<T> entityQuery(Class<T> type) {
    return new EntityQueryLoggerImpl<T>(type, TargetTaskLogger.log) {
      @Override
      public void close(List<LogEvent> entries) {
        messages.addAll(entries);
      }
      @Override
      public EntityQueryLogger<T> queryOk(RowSet<Row> e) {
        inserted_count.put(type, inserted_count.getOrDefault(type, 0) + getTotalEntries(e));
        return super.queryOk(e);
      }
    };
  }

  public void fail(Throwable e) {
    messages.add(LogEvent.builder()
        .level(LogEventLevel.ERROR)
        .props(Map.of(
            "text", "Failed to retrieve conversion data",
            "error", e.getMessage(),
            "stack", ExceptionUtils.getStackTrace(e)
        ))
        .build());
    log.error("\r\n{}", EntityQueryLogger.generateLog(messages), e);
  }
  
  public void skipRole(SourceRole e) {
    final var type = ImmutableSourceRole.class;
    inserted_count.put(type, inserted_count.getOrDefault(type, 0) + 1);
    messages.add(LogEvent.builder()
        .level(LogEventLevel.INFO)
        .props(Map.of(
            "text", "Skipping role for task, already exists",
            "taskId", String.valueOf(e.getTask_id()),
            "role", e.getAssigned_roles()
        ))
        .build());
  }
  
  public void ok(SourceTasks e) {
    final var errorsPresent = messages.stream()
        .filter(t -> t.getLevel() == LogEventLevel.ERROR)
        .count();
      
    if(errorsPresent > 0) {
      messages.add(LogEvent.builder()
          .level(LogEventLevel.ERROR)
          .props(Map.of(
              "text", "retrieved conversion types WITH ERRORS",
              "total errors", String.valueOf(errorsPresent)
          ))
          .build());
      log.error("\r\n{}", EntityQueryLogger.generateLog(messages), e);
    } else if(log.isInfoEnabled()) {
      messages.add(LogEvent.builder()
          .level(LogEventLevel.INFO)
          .props(Map.of(
              "text", "successfully inserted conversion data",
              "commits inserted", String.valueOf(inserted_count.getOrDefault(GrimCommit.class, 0)),
              "missions inserted", String.valueOf(inserted_count.getOrDefault(GrimMission.class, 0)),
              "assignments inserted", String.valueOf(inserted_count.getOrDefault(GrimAssignment.class, 0)),
              "assignment role's skipped", String.valueOf(inserted_count.getOrDefault(ImmutableSourceRole.class, 0)),
              "labels inserted", String.valueOf(inserted_count.getOrDefault(GrimMissionLabel.class, 0)),
              "remarks inserted", String.valueOf(inserted_count.getOrDefault(GrimRemark.class, 0)),
              "processes inserted", String.valueOf(inserted_count.getOrDefault(ProcessEntity.class, 0))
              
              
          ))
          .build());
      log.info("\r\n{}", EntityQueryLogger.generateLog(messages), e);
    }
  }
}
