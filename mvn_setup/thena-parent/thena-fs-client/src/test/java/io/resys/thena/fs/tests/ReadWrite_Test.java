package io.resys.thena.fs.tests;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import io.resys.thena.api.actions.TenantActions.TenantOperationStatus;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.fs.tests.config.DbTestTemplate;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ReadWrite_Test extends DbTestTemplate {

  @Test
  public void createAndUpdateMission() {
    final CreatedTenant repo = getClient().tenants().createOneTenant()
        .name("ReadWrite_Test-1", StructureType.fs)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    
    log.debug("created repo {}", repo);
    Assertions.assertEquals(TenantOperationStatus.OK, repo.getStatus());
  }
}
