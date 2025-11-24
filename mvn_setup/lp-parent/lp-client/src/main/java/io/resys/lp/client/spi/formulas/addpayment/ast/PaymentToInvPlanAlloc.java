package io.resys.lp.client.spi.formulas.addpayment.ast;

import java.math.BigDecimal;
import java.util.List;

import io.resys.thena.contract.client.entities.InvPlan;
import io.resys.thena.contract.client.entities.InvPlanAlloc;
import io.resys.thena.ledger.client.entities.MoneyRequest;
import io.resys.thena.ledger.client.entities.Payment;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

public class PaymentToInvPlanAlloc {
 
  
  @Value
  public static class Expression {
    Payment payment;
    MoneyRequest moneyRequest; 
    InvPlan invPlan;
    InvPlanAlloc allocation;
  }
  
  @Data @Builder @Jacksonized
  public static class Node {
    private final List<String> logs;
    
    private final String paymentId;
    private final String invPlanId;
    private final BigDecimal paymentGrossAmount;
    private final BigDecimal paymentKappaPaymentFeeAmount;
    private final BigDecimal paymentNetAmount;

    private final BigDecimal allocatedShare;
    private final BigDecimal allocatedAmount;
    
    private final BigDecimal fundUnitPrice;
    private final BigDecimal fundUnitAmount;
    
    private final BigDecimal allocationGammaMortalityFee;
    private final BigDecimal allocationNetAmount;
    private final BigDecimal allocationProjectedValue;
  }
}
