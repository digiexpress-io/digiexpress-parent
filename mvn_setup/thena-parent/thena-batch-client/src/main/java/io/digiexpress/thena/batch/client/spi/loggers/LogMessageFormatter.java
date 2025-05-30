package io.digiexpress.thena.batch.client.spi.loggers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.event.Level;

import lombok.Getter;

public class LogMessageFormatter {
  private static final String yellowColor = "\033[33m";
  private static final String resetColor = "\033[0m";
  private static final String errorColor = "\033[0;31m";
  private static final String magentaColor = "\033[35m";
  
  private final StringBuilder messages = new StringBuilder();
  
  public LogMessageAppender append() {
    return new LogMessageAppender() {
      @Override
      public void build(Level level) {
        super.build(level);
        append(this.getCode(), this.getProps(), level, null);
      }
    };
  }
  
  public LogMessageFormatter append(String code, Map<String, String> props, Level level, Throwable throwable) {
    
    messages.append("  - code: ")
      .append(withColor("code", code))
      .append(System.lineSeparator());
    
    if(throwable != null) {
      messages
        .append("    ").append("error: ")
        .append(withColorError(throwable.getMessage()))
        .append(System.lineSeparator())

        .append("    ").append("exception").append(": ")
        .append(withColorError(throwable.getClass().getCanonicalName()))
        .append(System.lineSeparator());
    }
    
    
    final var msgProp = props.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase("message")).findFirst();
    if(msgProp.isPresent()) {
      messages.append("    ")
      .append(msgProp.get().getKey()).append(": ")
      .append(
          level == Level.ERROR ?
          withColorError(msgProp.get().getValue()) :
          withColor(msgProp.get().getKey(), msgProp.get().getValue())
      )
      .append(System.lineSeparator());
    }
    
    
    if(props != null) {
      try {
      props.entrySet().stream()
        .filter(e -> !e.getKey().equalsIgnoreCase("code"))
        .filter(e -> !e.getKey().equalsIgnoreCase("message"))
        .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
        .forEach(e -> {
          messages.append("    ")
            .append(e.getKey()).append(": ")
            .append(withColor(e.getKey(), e.getValue()))
            .append(System.lineSeparator());
          
      });
      } catch(Exception e) {e.printStackTrace();
        System.err.println("");
      }
    }
    
    return this;
  }
  
  private String withColorError(String value) {
    return errorColor + value + resetColor;    
  }
  
  private String withColor(String key, String value) {
    if("sql".equalsIgnoreCase(key)) {
      return magentaColor + value + resetColor;
    }
    if("code".equalsIgnoreCase(key)) {
      return yellowColor + value + resetColor;
    }
    if("batch".equalsIgnoreCase(key)) {
      //return yellowColor + value + resetColor;
    }
    if("stepExecutorStatus".equalsIgnoreCase(key)) {
      return magentaColor + value + resetColor;
    }
    if("status".equalsIgnoreCase(key)) {
      return magentaColor + value + resetColor;
    }
    if("instance".equalsIgnoreCase(key)) {
      return magentaColor + value + resetColor;
    }
    return value;    
  }
    
  public String build() {
    return messages.toString();
  }

  @Getter
  public static class LogMessageAppender {
    private final Map<String, String> props = new HashMap<>();
    private String code;
    private Level level;
    
    public LogMessageAppender getProps(Map<String, String> props) {
      if(props != null) {
        this.props.putAll(props);
      }
      return this;
    }
    public LogMessageAppender setCode(String code) {
      this.code = code;
      return this;
    }
    public void build(Level level) {
      this.level = level;
    }
  }

}
