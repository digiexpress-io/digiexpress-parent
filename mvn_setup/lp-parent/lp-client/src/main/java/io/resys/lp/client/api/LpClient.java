package io.resys.lp.client.api;

import java.time.LocalDate;
import java.util.function.Consumer;

import io.resys.lp.client.api.entities.AnyCalculation;
import io.resys.lp.client.api.entities.Envelope;
import io.resys.thena.contract.client.api.ContractClient;
import io.resys.thena.ledger.client.api.LedgerClient;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewPayment;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface LpClient {
  Actions actions();
  
  // add clients with given tenants
  LpClient with(ContractClient contracts, LedgerClient ledgers);

  
  interface Actions {
    RealCalculation realCalculation();
    ImaginaryCalculation imaginaryCalculation();
    PaymentMatching paymentMatching();
  }
  interface RealCalculation {
    RealCalculation accountId(String nameOrIdOrRef);
    RealCalculation startDate(@Nullable LocalDate localDate);
    // make sure to check for failures...
    Uni<Envelope<AnyCalculation>> build();
  }
  
  interface ImaginaryCalculation {
    
  }
  
  interface PaymentMatching {
    // auto-match figure out if this can be matched by whatever rules....
    PaymentMatching addPayment(Consumer<NewPayment> payment);
    
    // make sure to check for failures...
    Uni<Envelope<PaymentMatching>> build();
  }
}
