package io.digiexpress.thena.batch.client.api.executor;

import io.vertx.core.json.JsonObject;

public interface ExecutorLogger {
  
  void info(String format, JsonObject args);
  
  void warn(String format, JsonObject args);
  void warn(String format, JsonObject args, Throwable t);
  
  void error(String format, JsonObject args);
  void error(String format, JsonObject args, Throwable t);
}
