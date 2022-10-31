package io.digiexpress.client.tests.support;

import java.util.function.Consumer;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.dialob.client.api.DialobComposer;
import io.dialob.client.spi.DialobComposerImpl;
import io.digiexpress.client.api.ServiceClient;
import io.digiexpress.client.api.ServiceComposer;
import io.digiexpress.client.spi.ServiceComposerImpl;
import io.resys.hdes.client.api.HdesComposer;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.thestencil.client.api.StencilComposer;
import io.thestencil.client.spi.StencilComposerImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
  
  public TestCaseBuilder builder(String testcases) {
    return builder.testcases(testcases);
  }
  
  public ServiceClient client() {
    return builder.getClient();
  }
  
  public DialobComposer dialob(ServiceClient client) {
    return new DialobComposerImpl(client.getConfig().getDialob());
  }
  
  public StencilComposer stencil(ServiceClient client) {
    return new StencilComposerImpl(client.getConfig().getStencil());
  }
  
  public HdesComposer hdes(ServiceClient client) {
    return new HdesComposerImpl(client.getConfig().getHdes());
  }
  
  public ServiceComposer service(ServiceClient client) {
    return new ServiceComposerImpl(client);
  }
  
  public Consumer<Throwable> log() {
    return (ex) -> {
      log.error(ex.getMessage(), ex);
    }; 
  }
}
