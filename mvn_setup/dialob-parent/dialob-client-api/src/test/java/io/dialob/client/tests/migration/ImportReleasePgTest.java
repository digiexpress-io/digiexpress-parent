package io.dialob.client.tests.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import io.dialob.client.tests.migration.MigrationSupport.FormReleaseDocument;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ImportReleasePgTest {

  @Test
  public void createForms() {
    for(final var value : getRelease().getValues()) {
      final var json = new JsonObject(value.getCommands());
      final var type = json.getValue("type");
      
      if(type.equals("FORM")) {
        log.debug("form: {} - {}", value.getHash(), new JsonObject(value.getCommands()));
      } else if(type.equals("FORM_REV")) {
        log.debug("rev: {} - {}", value.getHash(), new JsonObject(value.getCommands()));        
      }

    }
  }
  

  public FormReleaseDocument getRelease() {
    try {
      final var input = new FileInputStream(new File("src/test/resources/migration_dump.txt"));
      return new MigrationSupport().read(input).getRelease();
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}
