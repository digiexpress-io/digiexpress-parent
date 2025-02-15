package io.digiexpress.mig.client.spi.loggers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.mig.client.api.SourceForms;
import io.digiexpress.mig.client.api.SourceForms.SourceForm;
import io.digiexpress.mig.client.api.SourceForms.SourceFormDocument;
import io.digiexpress.mig.client.api.SourceForms.SourceFormRev;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger.EntityQueryLoggerImpl;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger.LogEvent;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger.LogEventLevel;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TargetDialobLogger {
  private final List<LogEvent> messages = Collections.synchronizedList(new ArrayList<>());
  private final Map<Class<?>, Integer> inserted_count = Collections.synchronizedMap(new HashMap<>());
  
  
  public <T> EntityQueryLogger<T> entityQuery(Class<T> type) {
    return new EntityQueryLoggerImpl<T>(type, TargetDialobLogger.log) {
      @Override
      public void close(List<LogEvent> entries) {
        messages.addAll(entries);
      }
      @Override
      public EntityQueryLogger<T> queryOk(RowSet<Row> e) {
        inserted_count.put(type, e.size());
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
  
  public void ok(SourceForms e) {
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
              "forms inserted/skipped", getCount(SourceForm.class, e.getForms().size()),
              "form document inserted/skipped", getCount(SourceFormDocument.class, e.getFormDocument().size()),
              "form revs inserted/skipped", getCount(SourceFormRev.class, e.getFormRev().size()),
              "questionnaires inserted/skipped", getCount(Questionnaire.class, e.getQuestionnaires().size())
              
          ))
          .build());
      log.info("\r\n{}", EntityQueryLogger.generateLog(messages), e);
    }
  }


  
  private String getCount(Class<?> type, int origin) {
    final var inserted = inserted_count.getOrDefault(type, 0);
    return inserted + "/" + (origin - inserted);
  }
}
