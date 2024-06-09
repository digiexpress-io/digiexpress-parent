package io.thestencil.client.tests.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.spi.DbState;
import io.resys.thena.support.DocDbPrinter;

public class TestExporter {
  private final DbState state;

  public TestExporter(DbState state) {
    super();
    this.state = state;
  }

  public String print(Tenant repo) {
    return new DocDbPrinter(state).print(repo);
  }

  public static String toString(Class<?> type, String resource) {
    try {
      return IOUtils.toString(type.getClassLoader().getResource(resource), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
