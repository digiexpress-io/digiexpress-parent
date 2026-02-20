package io.digiexpress.eveli.mig.v6.baseline.logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.digiexpress.eveli.mig.v6.baseline.OldEnvir.OldEnvirObjects;
import io.digiexpress.eveli.mig.v6.baseline.OldGit.OldGitObjects;
import io.digiexpress.eveli.mig.v6.baseline.logger.SqlLogger.LogEvent;
import io.digiexpress.eveli.mig.v6.baseline.logger.SqlLogger.LogEventLevel;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class BaselineLogger {
  private final List<LogEvent> messages = Collections.synchronizedList(new ArrayList<>());
  
  public <T> SqlLogger<T> entityQuery(Class<T> type) {
    return new SqlLoggerImpl<T>(type, BaselineLogger.log) {
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
    log.error("\r\n{}", SqlLogger.generateLog(messages), e);
  }
  
  public void ok(OldGitObjects e) {
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
      log.error("\r\n{}", SqlLogger.generateLog(messages), e);
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
              "tree values", String.valueOf(e.getTrees().stream().flatMap(n -> n.getValues().stream()).count())
              
          ))
          .build());
      log.info("\r\n{}", SqlLogger.generateLog(messages), e);
    }
  }
  
  public void ok(OldEnvirObjects e) {
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
      log.error("\r\n{}", SqlLogger.generateLog(messages), e);
    } else if(log.isInfoEnabled()) {
      messages.add(LogEvent.builder()
          .level(LogEventLevel.INFO)
          .props(Map.of(
              "text", "successfully retrieved conversion data",
              "docs", String.valueOf(e.getDocs().size()),
              "branches", String.valueOf(e.getBranches().size())
          ))
          .build());
      log.info("\r\n{}", SqlLogger.generateLog(messages), e);
    }
  }
}
