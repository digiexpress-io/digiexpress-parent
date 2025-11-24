package io.resys.lp.client.spi.formulas.addpayment.ast;

import java.math.BigDecimal;

import io.resys.thena.ledger.client.entities.MoneyRequest;
import lombok.Value;

public class MoneyToLedger {
  
  @Value
  public static class Expression {
    MoneyRequest moneyRequest;
  }
  
  @Value
  public static class Node {
    BigDecimal allocated;
    BigDecimal inflow;
    BigDecimal outflow;
  }
  
}
