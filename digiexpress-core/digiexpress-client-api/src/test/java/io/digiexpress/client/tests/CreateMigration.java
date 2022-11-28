package io.digiexpress.client.tests;

import org.junit.jupiter.api.Test;

import io.digiexpress.client.tests.migration.WorkflowMigration;
import io.digiexpress.client.tests.support.PgProfile;
import io.digiexpress.client.tests.support.TestCase;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import lombok.extern.slf4j.Slf4j;

@org.junit.jupiter.api.Disabled
@Slf4j
@QuarkusTest
@TestProfile(PgProfile.class)
public class CreateMigration extends TestCase {
  
  
  @Test
  public void test() {
    final var client = client();
    
    //final var dialob = DialobMigration.builder().build().execute();
    //final var hdes = HdesMigration.builder().build().execute(client.getConfig().getHdes());
    //final var stencil = StencilMigration.builder().build().execute();
    final var workflows = WorkflowMigration.builder().build().execute();
  }
}
