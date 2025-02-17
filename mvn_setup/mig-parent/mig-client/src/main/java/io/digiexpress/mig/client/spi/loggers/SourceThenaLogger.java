package io.digiexpress.mig.client.spi.loggers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.digiexpress.mig.client.api.SourceThena;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger.EntityQueryLoggerImpl;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger.LogEvent;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger.LogEventLevel;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class SourceThenaLogger {
  private final List<LogEvent> messages = Collections.synchronizedList(new ArrayList<>());
  
  public <T> EntityQueryLogger<T> entityQuery(Class<T> type) {
    return new EntityQueryLoggerImpl<T>(type, SourceThenaLogger.log) {
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
  
  public void ok(SourceThena e) {
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
              "blobs", String.valueOf(e.getBlobs().size()),
              "branches", String.valueOf(e.getBranches().size()),
              "commits", String.valueOf(e.getCommits().size()),
              "tags", String.valueOf(e.getTags().size()),
              "trees", String.valueOf(e.getTrees().size()),
              "tree values", String.valueOf(e.getTreeValues().size())
              
          ))
          .build());
      log.info("\r\n{}", EntityQueryLogger.generateLog(messages), e);
    }
  }


}
