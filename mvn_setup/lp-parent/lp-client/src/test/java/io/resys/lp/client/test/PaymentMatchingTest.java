package io.resys.lp.client.test;

import org.junit.jupiter.api.Test;

import io.resys.lp.client.test.config.DbTestTemplate;
import io.resys.lp.product.spi.providers.Contract_Provider;


public class PaymentMatchingTest extends DbTestTemplate {

  
  @Test
  public void test() {
    final var savings = Contract_Provider.newSavings(
        getContractClient(), 
        getLedgerClient(), 
        "001"
      ).await().atMost(atMost);
    
    final var firstPaymentPlan = savings.getContract()
        .getPaymentPlans().iterator().next();
    
    final var firstPaymentDate = firstPaymentPlan.getPaymentPlanStartDate();
    
  }
}
