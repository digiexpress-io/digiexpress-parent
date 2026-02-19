package io.digiexpress.eveli.mig.v6.baseline.logger;

import java.util.List;
import java.util.Map;

import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.Builder;
import lombok.Data;

public interface SqlLogger<T> {
  SqlLogger<T> query(String sql);
  SqlLogger<T> query(String sql, List<Tuple> props);
  SqlLogger<T> queryFail(Throwable e);
  SqlLogger<T> queryOk(List<T> e);
  SqlLogger<T> queryOk(RowSet<Row> e);
  
  SqlLogger<T> mappingOk(T entity);
  SqlLogger<T> mappingFail(Row task, Exception e);
  
  
  @Data @Builder
  public class LogEvent {
    private final Map<String, String> props;
    private final LogEventLevel level;
  }
  
  public enum LogEventLevel {
    INFO, ERROR, WARN, DEBUG
  }
  
  public static String generateLog(List<LogEvent> messages) {

    final String yellowColor = "\033[33m";
    final String resetColor = "\033[0m";
    final String errorColor = "\033[0;31m";
    final String magentaColor = "\033[35m";
    final var result = new StringBuilder("events:").append(System.lineSeparator());
    var index = 0;
    for(final var event : messages) {
      
      final boolean isLast = index++ == messages.size() -1;
      
      final var mainColor = event.getLevel() == LogEventLevel.ERROR ? errorColor : resetColor;
      result.append("  - ").append(mainColor).append(event.getLevel()).append(resetColor).append(":").append(System.lineSeparator());
      event.getProps().entrySet().forEach(e -> {
        final String typeColor;
        
        switch (e.getKey()) {
          case "sql": {
            typeColor = yellowColor; 
            break;
          }
          default: {
            if(isLast) {
              typeColor = magentaColor;              
            } else {
              typeColor = resetColor;
            }
            
          };
        }
        
        result.append("    ").append(e.getKey()).append(": ")
          .append(typeColor)
          .append(e.getValue()).append(resetColor)
          .append(System.lineSeparator());
      });
      
      result.append(resetColor);
    }
    return result.toString();
  }
}