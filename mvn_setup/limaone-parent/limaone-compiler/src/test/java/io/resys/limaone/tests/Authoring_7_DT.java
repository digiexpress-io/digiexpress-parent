package io.resys.limaone.tests;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.program.Program.ProgramStatus;
import io.resys.limaone.spi.compiler.CompilerImpl;
import io.resys.limaone.spi.runtime.DefaultEnvironmentProperties;
import io.resys.limaone.spi.runtime.DefaultEnvironmentProperties.ModelDbConfig;
import io.resys.limaone.tests.support.DbSupport;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Authoring_7_DT extends DbSupport {
  private static final CompilerImpl compiler = new CompilerImpl(DefaultEnvironmentProperties.builder()
      .dbConfig(ModelDbConfig.external(() -> { throw new RuntimeException(); }))
      .defaultTenantName("Authoring_7_DT")
      .build());

  @Test
  public void createOneDt() {
    final var authoring = new AuthoringImpl(createConfig());
    
    final var dt = authoring.newModel()
        .newDecisionTable()
        .props(props -> props.name("create_any_dt").desc("").nodes(Collections.emptyList()))
        .buildSync();
        
    final var world = authoring.worldQuery().findAll().await().atMost(atMost);
    final var bundle = compiler.compile(world).build().getBundle();
    
    Assertions.assertEquals(ProgramStatus.UP, bundle.queryDecisions().id(dt.getId()).getOne().getStatus());
  }
}


