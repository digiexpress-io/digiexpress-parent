package io.digiexpress.client.tests.support;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class TestCase {
  @Inject io.vertx.mutiny.pgclient.PgPool pgPool;
  private TestCaseBuilder builder;
  
  @BeforeEach
  public void setUp() {
    builder = new TestCaseBuilder(pgPool);
  }
  
  @AfterEach
  public void tearDown() {
    builder = null;
  }
  
  public TestCaseBuilder builder() {
    return builder;
  }
}
