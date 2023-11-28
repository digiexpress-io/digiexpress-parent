package io.resys.thena.tasks.dev.app;


import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NameMatchingTest {
  
  @Test
  public void matchName() {
    final var version_a = "Tuomas";
    final var version_b = "Toomas";
    final var diff = StringUtils.getLevenshteinDistance(version_a, version_b);
    
    log.debug("difference is: " + diff);
  }
}
