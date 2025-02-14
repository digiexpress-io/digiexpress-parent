package io.digiexpress.mig.client.spi.loggers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.digiexpress.mig.client.api.SourceDbClient.SourceDbTasks;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger.EntityQueryLoggerImpl;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger.LogEvent;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger.LogEventLevel;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class SourceDbTaskQueryLogger {
  private final List<LogEvent> messages = Collections.synchronizedList(new ArrayList<>());
  
  public <T> EntityQueryLogger<T> entityQuery(Class<T> type) {
    return new EntityQueryLoggerImpl<T>(type, SourceDbTaskQueryLogger.log) {
      @Override
      public void close(List<LogEvent> entries) {
        messages.addAll(entries);
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
  
  public void ok(SourceDbTasks e) {
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
              "text", "successfully retrieved conversion data",
              "tasks", String.valueOf(e.getTasks().size()),
              "processes", String.valueOf(e.getProcesses().size()),
              "comments", String.valueOf(e.getComments().values().stream().flatMap(v -> v.stream()).count()),
              "keywords", String.valueOf(e.getKeywords().values().stream().flatMap(v -> v.stream()).count()),
              "roles", String.valueOf(e.getRoles().values().stream().flatMap(v -> v.stream()).count()),
              "access", String.valueOf(e.getAccess().values().stream().flatMap(v -> v.stream()).count()),
              "links", String.valueOf(e.getLinks().values().stream().flatMap(v -> v.stream()).count())
              
          ))
          .build());
      log.info("\r\n{}", EntityQueryLogger.generateLog(messages), e);
    }
  }


}
