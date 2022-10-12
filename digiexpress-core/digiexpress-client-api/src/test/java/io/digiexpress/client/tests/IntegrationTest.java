package io.digiexpress.client.tests;

import org.junit.jupiter.api.Test;

import io.digiexpress.client.tests.support.PgProfile;
import io.digiexpress.client.tests.support.TestCase;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(PgProfile.class)
public class IntegrationTest extends TestCase {
  
  @Test
  public void init() {
//    final var api = null;
//    final var formId = api.form();
//    
//    api.article("# first topic").withActivity("Contact Us", "contact-activity", formId);
//    
//    
//    api.getContent();
//    
//    
  }
}
