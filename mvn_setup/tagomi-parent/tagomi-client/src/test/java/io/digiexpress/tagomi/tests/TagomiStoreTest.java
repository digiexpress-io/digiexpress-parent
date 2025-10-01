package io.digiexpress.tagomi.tests;

import org.junit.jupiter.api.Test;

import io.digiexpress.tagomi.tests.config.PgProfile;
import io.digiexpress.tagomi.tests.config.PgTestTemplate;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(PgProfile.class)
public class TagomiStoreTest extends PgTestTemplate {
  

  @Test
  public void test1() {
    final var repo = getPersistence("test1");
    
    
    
  }
}
