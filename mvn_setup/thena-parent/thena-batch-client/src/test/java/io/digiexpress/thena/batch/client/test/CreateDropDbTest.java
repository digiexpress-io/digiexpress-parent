package io.digiexpress.thena.batch.client.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.digiexpress.thena.batch.client.test.config.DbTestTemplate;
import io.digiexpress.thena.batch.client.test.config.PgProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import lombok.extern.slf4j.Slf4j;


@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class CreateDropDbTest extends DbTestTemplate {


  @Test
  public void createDropDb() {
    
    TenantCommitResult repo = getClient().manageTenants().commit()
        .name("my-batch-tenant")
        .build()
        .await().atMost(atMost);
    log.debug("created batch tenant {}", repo);
    
    
    getClient().manageTenants().find().id(repo.getRepo().getId()).delete().await().atMost(atMost);
    Assertions.assertEquals(CommitStatus.OK, repo.getStatus());
  }
}
