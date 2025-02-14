package io.digiexpress.mig.client.spi.loggers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.mig.client.api.SourceDbClient.FormFilter;
import io.digiexpress.mig.client.api.SourceDbClient.SourceDbDialob;
import io.digiexpress.mig.client.api.SourceDbClient.SourceDbFormDocument;
import io.digiexpress.mig.client.api.SourceDbClient.SourceDbFormRev;
import io.digiexpress.mig.client.api.SourceDbClient.SourceDbQuestionnaire;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger.EntityQueryLoggerImpl;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger.LogEvent;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger.LogEventLevel;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class SourceDbDialobQueryLogger {
  private final List<LogEvent> messages = Collections.synchronizedList(new ArrayList<>());
  
  public <T> EntityQueryLogger<T> entityQuery(Class<T> type) {
    return new EntityQueryLoggerImpl<T>(type, SourceDbDialobQueryLogger.log) {
      @Override
      public void close(List<LogEvent> entries) {
        messages.addAll(entries);
      }
    };
  }
  
  
  
  
  public void formRevNotFound(SourceDbFormDocument filter, SourceDbQuestionnaire q) {
    if(q == null) {
      if(log.isDebugEnabled()) {
        messages.add(LogEvent.builder()
            .level(LogEventLevel.DEBUG)
            .props(Map.of(
                "text", "form rev was not found",
                "form document id", filter.getId()
            ))
            .build());
      }
      return;
    }
    
    messages.add(LogEvent.builder()
        .level(LogEventLevel.ERROR)
        .props(Map.of(
            "text", "form rev was not found",
            "form document id", filter.getId()
        ))
        .build());
  }
  
  public void formRevMoreThenOneRev(SourceDbFormDocument filter, List<SourceDbFormRev> revs, SourceDbQuestionnaire q) {
    if(q == null) {
      
      if(log.isDebugEnabled()) {
        messages.add(LogEvent.builder()
            .level(LogEventLevel.DEBUG)
            .props(Map.of(
                "text", "found more then one form revision",
                "form document id", filter.getId(),
                "form rev names", String.join(", ", revs.stream().map(s -> s.getName()).toList())
            ))
            .build());
      }
      return;
    }
    
    
    messages.add(LogEvent.builder()
        .level(LogEventLevel.ERROR)
        .props(Map.of(
            "text", "found more then one form revision",
            "form document id", filter.getId(),
            "form rev names", String.join(", ", revs.stream().map(s -> s.getName()).toList())
        ))
        .build());
  }
  
  
  
  public void formNotFound(FormFilter filter) {
    messages.add(LogEvent.builder()
        .level(LogEventLevel.ERROR)
        .props(Map.of(
            "text", "form was not found based on wk filter",
            "form id", filter.getFormId().toString(),
            "form name", filter.getFormName(),
            "form tag", filter.getFormTag()
        ))
        .build());
  }
  
  public void questionnaireNotFound(String id) {
    messages.add(LogEvent.builder()
        .level(LogEventLevel.ERROR)
        .props(Map.of(
            "text", "questionnaire was required, but could not be found",
            "questionnaire id", id
        ))
        .build());
  }
  
  public void questionnaireNotUsed(SourceDbQuestionnaire q) {
    if(!log.isDebugEnabled()) {
      return;
    }
    final var meta = q.getData().map(json -> json.mapTo(Questionnaire.class));
    
    messages.add(LogEvent.builder()
        .level(LogEventLevel.DEBUG)
        .props(Map.of(
            "text", "Skipping questionnaire because it's not used",
            "questionnaire id", q.getId(),
            "form id", meta.map(e -> e.getMetadata().getFormId()).toString(),
            "form name", meta.map(e -> e.getMetadata().getFormName()).toString()
        ))
        .build());

  }
  
  public void questionnaireFormNotFound(SourceDbQuestionnaire q) {
    final var meta = q.getData().map(json -> json.mapTo(Questionnaire.class));
    
    messages.add(LogEvent.builder()
        .level(LogEventLevel.ERROR)
        .props(Map.of(
            "text", "Skipping questionnaire because form document is not found",
            "questionnaire id", q.getId(),
            "form id", meta.map(e -> e.getMetadata().getFormId()).toString(),
            "form name", meta.map(e -> e.getMetadata().getFormName()).toString()
        ))
        .build());

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
  
  public void ok(SourceDbDialob e) {
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
              "forms", String.valueOf(e.getForms().size()),
              "form document", String.valueOf(e.getFormDocument().size()),
              "form revs", String.valueOf(e.getFormRev().size()),
              "questionnaires", String.valueOf(e.getQuestionnaires().size())
              
          ))
          .build());
      log.info("\r\n{}", EntityQueryLogger.generateLog(messages), e);
    }
  }


}
