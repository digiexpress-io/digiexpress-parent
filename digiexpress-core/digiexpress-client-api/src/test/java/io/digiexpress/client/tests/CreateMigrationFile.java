package io.digiexpress.client.tests;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.digiexpress.client.api.ImmutableCreateMigration;
import io.digiexpress.client.tests.migration.DialobMigration;
import io.digiexpress.client.tests.migration.HdesMigration;
import io.digiexpress.client.tests.migration.StencilMigration;
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
public class CreateMigrationFile extends TestCase {
  private final Duration atMost = Duration.ofMillis(100000);
  
  @Test
  public void test() {
    final var client = client()
        .repo().repoService("test-prj-1-migration").create()
        .await().atMost(atMost);
  
    // load up the content
    final var dialob = DialobMigration.builder().build().execute();
    final var hdes = HdesMigration.builder().build().execute(client.getConfig().getHdes());
    final var stencil = StencilMigration.builder().build().execute();
    final var services = WorkflowMigration.builder().build().execute(hdes, dialob);
    
    final var migration = ImmutableCreateMigration.builder()
        .forms(dialob.getForms())
        .formRevs(dialob.getRevs())
        .hdes(hdes.getStoreState())
        .stencil(stencil)
        .services(services)
        .build();
    super.writeOutput(migration);
    
    //test drive the migration
    final var composer = service(client);
    final var result = composer.create().migrate(migration).await().atMost(atMost);
    
  }
}
