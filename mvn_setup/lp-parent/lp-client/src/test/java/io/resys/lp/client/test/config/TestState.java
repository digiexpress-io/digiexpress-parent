package io.resys.lp.client.test.config;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import io.resys.thena.contract.client.spi.ContractClientImpl;
import io.resys.thena.ledger.client.spi.LedgerClientImpl;
import io.resys.thena.storesql.PgErrors;
import io.vertx.mutiny.sqlclient.Pool;

public class TestState {
  private static AtomicInteger INDEX = new AtomicInteger(1);
  private final io.vertx.mutiny.sqlclient.Pool pgPool;
  private final Duration atMost = Duration.ofMinutes(1);
  
  private ContractClientImpl client_contract;
  private LedgerClientImpl ledger_contract;
  
  
  public TestState(Pool pgPool) {
    super();
    this.pgPool = pgPool;
    
    allocate();
    create();
  }
  
  private void allocate() {
    this.client_contract = ContractClientImpl.create()
      .tenantName("contract_" + INDEX.incrementAndGet())
      .client(pgPool)
      .errorHandler(new PgErrors())
      .build();

    this.ledger_contract = LedgerClientImpl.create()
      .tenantName("ledger_" + INDEX.incrementAndGet())
      .client(pgPool)
      .errorHandler(new PgErrors())
      .build();
  }
  
  private void create() {
    this.client_contract
      .tenants().commit()
      .build()
      .await().atMost(atMost);
    this.ledger_contract
      .tenants().commit()
      .build()
      .await().atMost(atMost);
  }

  public ContractClientImpl getClientContract() {
    return client_contract;
  }

  public LedgerClientImpl getLedgerContract() {
    return ledger_contract;
  }
}
