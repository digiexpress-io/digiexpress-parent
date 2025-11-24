package io.resys.lp.client.spi.formulas.addpayment.ast;

import java.math.BigDecimal;

import io.resys.thena.contract.client.entities.InvPlan;
import io.resys.thena.contract.client.entities.InvPlanAlloc;
import io.resys.thena.ledger.client.entities.MoneyRequest;
import io.resys.thena.ledger.client.entities.Payment;
import lombok.Value;

public class PaymentToInvPlanAlloc {
 
  
  @Value
  public static class Expression {
    Payment payment;
    MoneyRequest moneyRequest; 
    InvPlan invPlan;
    InvPlanAlloc allocation; 
  }
  
  @Value
  public static class Node {
    BigDecimal allocated; 
  }
}
