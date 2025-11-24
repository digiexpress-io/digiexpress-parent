package io.resys.lp.client.api;

import java.time.LocalDate;
import java.util.function.Consumer;

import io.resys.lp.client.api.entities.AllocatedPayments;
import io.resys.lp.client.api.entities.AnyCalculation;
import io.resys.lp.client.api.entities.Envelope;
import io.resys.thena.contract.client.api.ContractClient;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.ledger.client.api.LedgerClient;
import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewPayment;
import io.smallrye.mutiny.Uni;

public interface LpClient {
  Actions actions();
  
  // add clients with given tenants
  LpClient with(ContractClient contracts, LedgerClient ledgers);

  
  interface Actions {
    CalculatePayment calculatePayment();
    MatchPayment matchPayment();
  }
  
  // Calculate any open matched payments
  interface CalculatePayment {
    CalculatePayment ledgerId(String ledgerId);
    CalculatePayment targetDate(LocalDate targetDate);
    CalculatePayment formula(CalculatePaymentFormula formula);
    
    // make sure to check for failures...
    Uni<Envelope<AnyCalculation>> build();
  }
  
  interface CalculatePaymentFormula {
    Uni<Envelope<AnyCalculation>> accept(FormulaContainer container);
  }
    
  interface FormulaContainer {
    ContractClient getContractClient();
    LedgerClient getLedgerClient();
    
    ContractContainer getContract();
    LedgerContainer getLedger();
    LocalDate getStartDate();
    LocalDate getToday();
  }
  
  
  interface MatchPayment {
    MatchPayment addHint(String contractIdOrRefOrEtc);
    
    // auto-match figure out if this can be matched by whatever rules....
    MatchPayment addPayment(Consumer<NewPayment> payment);
    
    // make sure to check for failures...
    Uni<Envelope<AllocatedPayments>> build();
  }
}
