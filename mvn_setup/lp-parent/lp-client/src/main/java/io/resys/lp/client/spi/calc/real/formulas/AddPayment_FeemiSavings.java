package io.resys.lp.client.spi.calc.real.formulas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import io.resys.lp.client.spi.calc.CalculationFactory.CalculationContext;
import io.resys.lp.client.spi.calc.CalculationFactory.LedgerCalculator;
import io.resys.thena.contract.client.entities.InvPlan;
import io.resys.thena.contract.client.entities.InvPlanAlloc;
import io.resys.thena.ledger.client.api.LedgerCommitActions.OneLedgerEnvelope;
import io.resys.thena.ledger.client.api.ThenaLedgerMergeObject.MergeLedger;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewBlackBook;
import io.resys.thena.ledger.client.entities.BlackBook;
import io.resys.thena.ledger.client.entities.MoneyRequest;
import io.resys.thena.ledger.client.entities.MoneyRequest.MoneyRequestStatus;
import io.resys.thena.ledger.client.entities.MoneyRequest.MoneyRequestType;
import io.resys.thena.ledger.client.entities.Payment;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AddPayment_FeemiSavings implements LedgerCalculator {

  private final CalculationContext ctx;
  private final List<String> log = new ArrayList<>();
  
  private MergeLedger ledger;
  private NewBlackBook newBlackBook;
  private BlackBook lastBlackBook;

  
  // FEEMI_SAV_001 product parameters
  private static final BigDecimal KAPPA = new BigDecimal("0.02"); // 2% payment fee
  private static final BigDecimal KAPPA_REDUCED = BigDecimal.ZERO; // 0% monthly fee
  private static final BigDecimal GAMMA = new BigDecimal("0.001"); // mortality rate from death cover
  private static final BigDecimal MU = new BigDecimal("0.05"); // estimated return rate

  
  public Uni<OneLedgerEnvelope> accept() {
    
    // Iterate through open money requests
    final var openRequests = ctx.getLedger().getMoneyRequests().stream()
        .filter(request -> request.getRequestStatus() == MoneyRequestStatus.OPEN)
        .filter(request -> request.getRequestType() == MoneyRequestType.ADD_PAYMENT)
        .toList();
    
     final var sortedBb = new ArrayList<>(ctx.getLedger().getBlackBooks());
     sortedBb.sort(BlackBook.COMPARATOR);
     this.lastBlackBook = sortedBb.getFirst();
     
    return ctx.getLedgerClient().withTenant().commit().modifyOneLedger()
      .commitMessage("+ payment calculation")
      .commitAuthor(AddPayment_FeemiSavings.class.getSimpleName())
      .ledgerId(ctx.getLedger().getLedger().getId())
      .modifyLedger(ledger -> {
        // start 
        this.ledger = ledger;
        
        // execute
        visitLedger(openRequests);
        
        // end
        this.ledger.build();
      })
      .build();
  }
  
  private void visitLedger(List<MoneyRequest> openRequests) {
    ledger.addBlackBook(bb -> {
      // start 
      this.newBlackBook = bb;

      // execute
      var total = BigDecimal.ZERO;
      for (final var request : openRequests) {
        total = total.add(visitPayment(request));
      }

      // end
      this.newBlackBook
        .amount(total)
        .date(LocalDate.now())
        .type("INCOMING_PAYMENT")
        .build();
    });
  }
  
      
  private BigDecimal visitPayment(MoneyRequest moneyRequest) {
    
    // Find related payment
    final var payment = ctx.getLedger().getPayments().stream()
        .filter(p -> (
            p.getExternalId().equals(moneyRequest.getExternalId().orElse("")) ||
            p.getId().equals(moneyRequest.getPaymentId().orElse(""))
            ))
        .findFirst()
        .orElse(null);
    
    if (payment == null) {
      return BigDecimal.ZERO;
    }
    
    // Cross join with investment plans
    final var balance = BigDecimal.ZERO;
    for (final var invPlan : ctx.getContract().getInvPlans()) {
      balance.add(visitInvPlan(payment, moneyRequest, invPlan));
    }
    
    ledger.modifyMoneyRequest(moneyRequest.getId(), change -> {
      change.status(MoneyRequestStatus.CLOSED).build();
    });
    
    return balance;
  }
  
  private BigDecimal visitInvPlan(Payment payment, MoneyRequest moneyRequest, InvPlan invPlan) {
    final var grossAmount = moneyRequest.getRequestAmount();
    final var paymentFee = grossAmount.multiply(KAPPA).setScale(2, RoundingMode.HALF_UP);
    final var netAmount = grossAmount.subtract(paymentFee);
    
    // Get allocations for this investment plan
    final var allocations = ctx.getContract().getInvPlanAllocations().get(invPlan.getId());
    if (allocations == null || allocations.isEmpty()) {
      return BigDecimal.ZERO;
    }
    
    final var feeLog = String.format("Fee calculation: %s * %s = %s", grossAmount, KAPPA, paymentFee);
    log.add(feeLog);

    
    for (final var allocation : allocations) {
      final var allocated = visitInvPlanAlloc(payment, moneyRequest, invPlan, allocation, netAmount);
      
      newBlackBook.addBlackBookDetail(bbd -> bbd
          .type("PAYMENT_DETAIL")
          .subType("PAYMENT_AMOUNT_ALLOC")
          .amount(allocated)
          .paymentId(payment.getId())
          .targetId(allocation.getId())
          .body(JsonObject.of(
              "kappa_log", feeLog,
              "kappa", paymentFee,
              "net_amount", netAmount,
              "gross_amount", grossAmount
          ))
          .build());
    }
    
    // Add payment fee calculation
    
    return netAmount;
  }
  
  private BigDecimal visitInvPlanAlloc(Payment payment, MoneyRequest moneyRequest, InvPlan invPlan, 
                                 InvPlanAlloc allocation, BigDecimal netAmount) {
    
    final var allocationAmount = netAmount.multiply(allocation.getInvPlanAllocPercentage())
        .setScale(2, RoundingMode.HALF_UP);
    
    // Find unit price for this allocation
    final var unitPrice = ctx.getLedger().getUnitPrices().stream()
        .filter(up -> up.getExternalId().equals(allocation.getInvPlanAllocCode()))
        .findFirst()
        .map(up -> up.getUnitValue())
        .orElse(BigDecimal.ONE);
    
    final var units = allocationAmount.divide(unitPrice, 6, RoundingMode.HALF_UP);
    
    visitFormula(payment, moneyRequest, invPlan, allocation, allocationAmount, units, unitPrice);
    
    return allocationAmount;
  }
  
  private void visitFormula(Payment payment, MoneyRequest moneyRequest, InvPlan invPlan,
                           InvPlanAlloc allocation, BigDecimal amount, BigDecimal units, 
                           BigDecimal unitPrice) {
    
    // FEEMI_SAV_001 formula calculation
    final var mortalityCharge = amount.multiply(GAMMA).setScale(2, RoundingMode.HALF_UP);
    final var netInvestment = amount.subtract(mortalityCharge);
    final var projectedValue = netInvestment.multiply(BigDecimal.ONE.add(MU)).setScale(2, RoundingMode.HALF_UP);
    
    log.add(String.format("FEEMI_SAV_001 - Payment: %s, Plan: %s, Alloc: %s%% to %s", 
        payment.getExternalId(), invPlan.getInvPlanCode(), 
        allocation.getInvPlanAllocPercentage().multiply(new BigDecimal("100")), allocation.getInvPlanAllocPercentage()));
    
    log.add(String.format("Amount: %s, Units: %s @ %s, Mortality: %s, Net Investment: %s, Projected: %s",
        amount, units, unitPrice, mortalityCharge, netInvestment, projectedValue));
  }
}