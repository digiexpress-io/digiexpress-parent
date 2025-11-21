package io.resys.lp.client.spi.calc;

import java.time.LocalDate;

import io.resys.lp.client.spi.calc.real.formulas.AddPayment_FeemiSavings;
import io.resys.thena.contract.client.api.ContractClient;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.ledger.client.api.LedgerClient;
import io.resys.thena.ledger.client.api.LedgerCommitActions.OneLedgerEnvelope;
import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
public class CalculationFactory {
  private final ContractClient contractClient;
  private final LedgerClient ledgerClient;
  

  public interface LedgerCalculator {
    Uni<OneLedgerEnvelope> accept();
  }
  
  @Value
  public static class CalculationContext {
    ContractClient contractClient;
    LedgerClient ledgerClient;
    
    ContractContainer contract;
    LedgerContainer ledger;
    LocalDate startDate;
  }

  public Builder builder() {
    return new Builder(contractClient, ledgerClient);
  }
  
  @RequiredArgsConstructor
  public static class Builder {
    private final ContractClient contractClient;
    private final LedgerClient ledgerClient;
    
    private ContractContainer contract;
    private LedgerContainer ledger;
    private LocalDate startDate;

    public Builder contract(ContractContainer contract) {
      this.contract = contract;
      return this;
    }
    public Builder ledger(LedgerContainer ledger) {
      this.ledger = ledger;
      return this;
    }
    public Builder startDate(LocalDate startDate) {
      this.startDate = startDate;
      return this;
    }    
    
    public LedgerCalculator incomingPayments() {
      RepoAssert.notNull(contract, () -> "contract must be defined!");
      RepoAssert.notNull(ledger, () -> "ledger must be defined!");
      
      final var context = new CalculationContext(contractClient, ledgerClient, contract, ledger, startDate);
      final var resolver = context.getContract().getContract().getContractSubType().orElse("");
      
      switch (resolver) {
        case "FEEMI_SAVINGS": return new AddPayment_FeemiSavings(context);
        default: throw new IllegalArgumentException("Unexpected value: " + resolver);
      }
      
    }
  }  
}
