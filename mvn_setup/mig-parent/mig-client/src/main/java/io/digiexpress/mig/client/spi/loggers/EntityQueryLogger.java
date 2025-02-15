package io.digiexpress.mig.client.spi.loggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public interface EntityQueryLogger<T> {
  EntityQueryLogger<T> query(String sql);
  EntityQueryLogger<T> query(String sql, List<Tuple> props);
  EntityQueryLogger<T> queryFail(Throwable e);
  EntityQueryLogger<T> queryOk(List<T> e);
  EntityQueryLogger<T> queryOk(RowSet<Row> e);
  
  EntityQueryLogger<T> mappingOk(T entity);
  EntityQueryLogger<T> mappingFail(Row task, Exception e);
  
  
  @Data @Builder
  public class LogEvent {
    private final Map<String, String> props;
    private final LogEventLevel level;
  }
  
  public enum LogEventLevel {
    INFO, ERROR, WARN, DEBUG
  }
  
  public static String generateLog(List<LogEvent> messages) {
    final var result = new StringBuilder("events:").append(System.lineSeparator());
    for(final var event : messages) {
      result.append("  - ").append(event.getLevel()).append(":").append(System.lineSeparator());
      event.getProps().entrySet().forEach(e -> 
        result.append("    ").append(e.getKey()).append(": ").append(e.getValue()).append(System.lineSeparator())
      );
    }
    return result.toString();
  }

  @RequiredArgsConstructor
  abstract class EntityQueryLoggerImpl<T> implements EntityQueryLogger<T> {
    private final Class<T> type;
    private final Logger log;
    private final List<LogEvent> messages = new ArrayList<>();
    
    public abstract void close(List<LogEvent> entries);
    
    @Override
    public EntityQueryLogger<T> query(String sql) {
      if(!log.isInfoEnabled()) {
        return this;
      }
      
      messages.add(LogEvent.builder()
        .level(LogEventLevel.INFO)
        .props(Map.of(
            "text", "executing SQL query",
            "conversion type", type.getSimpleName(),
            "sql", sql
        ))
        .build());
      
      return this;
    }

    @Override
    public EntityQueryLogger<T> queryFail(Throwable e) {
      messages.add(LogEvent.builder()
        .level(LogEventLevel.ERROR)
        .props(Map.of(
            "text", "failed to fully to retrive conversion type relations",
            "conversion type", type.getSimpleName(),
            "error", e.getMessage(),
            "stack", ExceptionUtils.getStackTrace(e)
        ))
        .build());
      
      this.close(messages);
      return this;
    }

    
    @Override
    public EntityQueryLogger<T> query(String sql, List<Tuple> props) {
      messages.add(LogEvent.builder()
          .level(LogEventLevel.INFO)
          .props(Map.of(
              "text", "inserting entries",
              "sql", sql,
              "number of entries in query", String.valueOf(props.size()),
              "conversion type", type.getSimpleName()
          ))
          .build());
      return this;
    }
    
    @Override
    public EntityQueryLogger<T> queryOk(RowSet<Row> e) {
      messages.add(LogEvent.builder()
        .level(LogEventLevel.INFO)
        .props(Map.of(
            "text", "successfully inserted entries",
            "number of entries inserted", String.valueOf(e.size()),
            "conversion type", type.getSimpleName()
        ))
        .build());
      this.close(messages);
      return this;
    }
    
    @Override
    public EntityQueryLogger<T> queryOk(List<T> e) {

      final var errorsPresent = messages.stream()
        .filter(t -> t.getLevel() == LogEventLevel.ERROR)
        .count();
      
      if(errorsPresent > 0) {
        messages.add(LogEvent.builder()
            .level(LogEventLevel.ERROR)
            .props(Map.of(
                "text", "retrieved conversion types WITH ERRORS",
                "failed to convert entries", String.valueOf(errorsPresent),
                "successfully converted entries", String.valueOf(e.size()),
                "conversion type", type.getSimpleName()
            ))
            .build());

      } else if(log.isDebugEnabled()) {
        messages.add(LogEvent.builder()
            .level(LogEventLevel.DEBUG)
            .props(Map.of(
                "text", "successfully retrieved conversion types",
                "conversion type", type.getSimpleName()
            ))
            .build());
      }
      
      this.close(messages);
      return this;
    }

    @Override
    public EntityQueryLogger<T> mappingOk(T entity) {
      if(!log.isDebugEnabled()) {
        return this;
      }
      final var json = JsonObject.mapFrom(entity);
      final var taskId = Optional
        .ofNullable(json.getLong("task_id"))
        .orElse(json.getLong("id"));
      
      messages.add(LogEvent.builder()
          .level(LogEventLevel.DEBUG)
          .props(Map.of(
              "text", "succefully converted entry",
              "conversion type", type.getSimpleName(),
              "task id", String.valueOf(taskId) 
          ))
          .build());
      return this;
    }

    @Override
    public EntityQueryLogger<T> mappingFail(Row task, Exception e) {
    
      Long taskId = null;      
      try {
        taskId = task.getLong("task_id");
      } catch(Exception ex) {}

      try {
        if(taskId == null) {
          taskId = task.getLong("id");
        }
      } catch(Exception ex) {}
      
      messages.add(LogEvent.builder()
          .level(LogEventLevel.ERROR)
          .props(Map.of(
              "text", "failed to convert entry",
              "conversion type", type.getSimpleName(),
              "task id", String.valueOf(taskId),
              "error", e.getMessage(),
              "stack", ExceptionUtils.getStackTrace(e)
          ))
          .build());
      return this;
    }
  }
}