package io.digiexpress.mig.client.spi.loggers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.digiexpress.mig.client.api.SourceThena;
import io.digiexpress.mig.client.api.SourceThena.TreeValueExt;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger.EntityQueryLoggerImpl;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger.LogEvent;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger.LogEventLevel;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.Tag;
import io.resys.thena.api.entities.git.Tree;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TargetThenaLogger {
  private final List<LogEvent> messages = Collections.synchronizedList(new ArrayList<>());
  private final Map<Class<?>, Integer> inserted_count = Collections.synchronizedMap(new HashMap<>());
  
  
  public <T> EntityQueryLogger<T> entityQuery(Class<T> type) {
    return new EntityQueryLoggerImpl<T>(type, TargetThenaLogger.log) {
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
              "text", "successfully inserted conversion data",
              "blobs inserted", String.valueOf(inserted_count.getOrDefault(Blob.class, 0)),
              "commits inserted", String.valueOf(inserted_count.getOrDefault(Commit.class, 0)),
              "branches inserted", String.valueOf(inserted_count.getOrDefault(Branch.class, 0)),
              "tags inserted", String.valueOf(inserted_count.getOrDefault(Tag.class, 0)),
              "tree values inserted", String.valueOf(inserted_count.getOrDefault(TreeValueExt.class, 0)),
              "trees inserted", String.valueOf(inserted_count.getOrDefault(Tree.class, 0))
              
          ))
          .build());
      log.info("\r\n{}", EntityQueryLogger.generateLog(messages), e);
    }
  }
}
