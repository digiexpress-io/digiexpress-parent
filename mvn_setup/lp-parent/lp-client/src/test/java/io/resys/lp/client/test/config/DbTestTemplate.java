package io.resys.lp.client.test.config;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.resys.lp.client.api.LpClient;
import io.resys.thena.contract.client.api.ContractClient;
import io.resys.thena.ledger.client.api.LedgerClient;
import io.resys.thena.test.ThenaTest;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ThenaTest
public class DbTestTemplate {
  
  private TestState client;
  protected io.vertx.mutiny.sqlclient.Pool pgPool;
  protected static Duration atMost = Duration.ofMinutes(1);
  

  @BeforeEach
  public void setUp(io.vertx.mutiny.sqlclient.Pool pgPool) {
    this.pgPool = pgPool;
    this.client = new TestState(pgPool);
  }

  @AfterEach
  public void tearDown() {
  }
  public LpClient getLpClient() {
    return null;
  }
  public ContractClient getContractClient() {
    return client.getClientContract();
  }
  public LedgerClient getLedgerClient() {
    return client.getLedgerContract();    
  }
}