package io.resys.thena.ledger.client.api;



import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.api.envelope.ThenaContainer;
import io.resys.thena.ledger.client.entities.BlackBook;
import io.resys.thena.ledger.client.entities.BlackBookDetail;
import io.resys.thena.ledger.client.entities.Ledger;
import io.resys.thena.ledger.client.entities.LedgerEvent;
import io.resys.thena.ledger.client.entities.MoneyRequest;
import io.resys.thena.ledger.client.entities.Payment;
import io.resys.thena.ledger.client.entities.Projection;
import io.resys.thena.ledger.client.entities.ProjectionDetail;
import io.resys.thena.ledger.client.entities.Settlement;
import io.resys.thena.ledger.client.entities.SettlementPayment;
import io.resys.thena.ledger.client.entities.UnitPrice;

/**
 * Container objects that aggregate related contract entities together,
 * similar to GrimMissionContainer in the Grim domain.
 */
public interface ThenaLedgerContainers {

  @Value.Immutable
  @JsonSerialize(as = ImmutableLedgerContainer.class)
  @JsonDeserialize(as = ImmutableLedgerContainer.class)
  interface LedgerContainer extends ThenaContainer {
    Ledger getLedger();
    
    List<BlackBook> getBlackBooks();
    Map<String, List<BlackBookDetail>> getBlackBookDetails();
    List<LedgerEvent> getLedgerEvents();
    List<MoneyRequest> getMoneyRequest();
    List<Payment> getPayments();
    List<Projection> getProjections();
    Map<String, List<ProjectionDetail>> getProjectionDetails();
    
    List<Settlement> getSettlements();
    List<SettlementPayment> getSettlementPayments();
    List<UnitPrice> getUnitPrices();
  }
}