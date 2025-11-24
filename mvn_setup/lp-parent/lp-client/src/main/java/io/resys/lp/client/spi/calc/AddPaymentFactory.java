package io.resys.lp.client.spi.calc;

import io.resys.lp.client.api.LpClient.CalculatePaymentFormula;
import io.resys.lp.client.api.LpClient.FormulaContainer;
import io.resys.lp.client.api.entities.AnyCalculation;
import io.resys.lp.client.api.entities.Envelope;
import io.resys.lp.client.spi.formulas.addpayment.AddPayment_FeemiSavings;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AddPaymentFactory implements CalculatePaymentFormula {
  
  @Override
  public Uni<Envelope<AnyCalculation>> accept(FormulaContainer container) {

    final var resolver = container.getContract().getContract().getContractSubType().orElse("");
    
    switch (resolver) {
      case "FEEMI_SAVINGS": return new AddPayment_FeemiSavings().accept(container);
      default: throw new IllegalArgumentException("Unexpected value: " + resolver);
    }
  }  

}
