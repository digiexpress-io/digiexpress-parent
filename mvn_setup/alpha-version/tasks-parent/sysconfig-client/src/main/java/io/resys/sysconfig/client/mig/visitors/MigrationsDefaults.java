package io.resys.sysconfig.client.mig.visitors;

public class MigrationsDefaults {
  
  public static TableLog summary(String... cols) {    
    return new TableLog(cols);
  }
  
}
