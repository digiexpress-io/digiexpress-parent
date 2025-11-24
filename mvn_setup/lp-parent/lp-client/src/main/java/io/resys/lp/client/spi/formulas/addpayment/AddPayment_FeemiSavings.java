package io.resys.lp.client.spi.formulas.addpayment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.resys.lp.client.api.LpClient.CalculatePaymentFormula;
import io.resys.lp.client.api.LpClient.FormulaContainer;
import io.resys.lp.client.api.entities.AnyCalculation;
import io.resys.lp.client.api.entities.Envelope;
import io.resys.lp.client.api.entities.Envelope.EnvelopeStatus;
import io.resys.lp.client.api.entities.ImmutableEnvelope;
import io.resys.lp.client.api.entities.ImmutableLog;
import io.resys.lp.client.spi.formulas.addpayment.ast.MoneyToLedger;
import io.resys.lp.client.spi.formulas.addpayment.ast.PaymentToInvPlan;
import io.resys.lp.client.spi.formulas.addpayment.ast.PaymentToInvPlanAlloc;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.contract.client.entities.InvPlan;
import io.resys.thena.contract.client.entities.InvPlanAlloc;
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
public class AddPayment_FeemiSavings implements CalculatePaymentFormula {

  private FormulaContainer ctx;
  private MergeLedger ledger;
  private NewBlackBook newBlackBook;
  private BlackBook lastBlackBook;

  
  // FEEMI_SAV_001 product parameters
  private static final BigDecimal KAPPA = BigDecimal.ZERO; // 0% payment fee per spec
  private static final BigDecimal GAMMA = new BigDecimal("0.001"); // mortality rate from death cover
  private static final BigDecimal MU = new BigDecimal("0.05"); // estimated return rate

  
  public Uni<Envelope<AnyCalculation>> accept(FormulaContainer container) {
    
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
        visitAllOpenMoneyRquests(openRequests);
        
        // end
        this.ledger.build();
      }).build()
      
      // map to end result
      .onItem().transform(env -> ImmutableEnvelope.<AnyCalculation>builder()
          .status(env.getStatus() == CommitResultStatus.OK ? EnvelopeStatus.OK : EnvelopeStatus.ERROR)
          .addAllLogs(env.getMessages().stream().map(e -> ImmutableLog.builder()
              .exception(e.getException())
              .text(e.getText())
              .build()).toList())
          .addLogs(ImmutableLog.builder()
              .targetId(String.join(",", container.getLedger().getLedger().getId()))
              .text("Ledger calculated")
              .build())
          .object(null)
          .build());
  }
  
  private void visitAllOpenMoneyRquests(List<MoneyRequest> openRequests) {
    ledger.addBlackBook(bb -> {
      // start 
      this.newBlackBook = bb;

      // execute
      var allocated = BigDecimal.ZERO;
      for (final var request : openRequests) {
        final var node = visitMoneyToLedger(new MoneyToLedger.Expression(request));
        allocated = allocated.add(node.getAllocated());
      }

      // end
      this.newBlackBook
        .amount(allocated)
        .date(LocalDate.now())
        .type("INCOMING_PAYMENT")
        .build();
    });
  }
  
      
  private MoneyToLedger.Node visitMoneyToLedger(MoneyToLedger.Expression exp) {
    
    final var moneyRequest = exp.getMoneyRequest();
    
    // Find related payment
    final var payment = ctx.getLedger().getPayments().stream()
        .filter(p -> (
            p.getExternalId().equals(moneyRequest.getExternalId().orElse("")) ||
            p.getId().equals(moneyRequest.getPaymentId().orElse(""))
            ))
        .findFirst()
        .orElse(null);
    
    if (payment == null) {
      return new MoneyToLedger.Node(BigDecimal.ZERO);
    }
    
    // Cross join with investment plans
    final var balance = BigDecimal.ZERO;
    for (final var invPlan : ctx.getContract().getInvPlans()) {
      final var node = visitPaymentToInvPlan(new PaymentToInvPlan.Expression(payment, moneyRequest, invPlan));
      balance.add(node.getAllocated());
    }
    
    ledger.modifyMoneyRequest(moneyRequest.getId(), change -> {
      change.status(MoneyRequestStatus.CLOSED).build();
    });
    
    return new MoneyToLedger.Node(balance);
  }
  
  
  
  private PaymentToInvPlan.Node visitPaymentToInvPlan(PaymentToInvPlan.Expression exp) {
    
    final Payment payment = exp.getPayment(); 
    final MoneyRequest moneyRequest = exp.getMoneyRequest(); 
    final InvPlan invPlan = exp.getInvPlan();
    
    // Get allocations for this investment plan
    final var allocations = ctx.getContract().getInvPlanAllocations().get(invPlan.getId());
    if (allocations == null || allocations.isEmpty()) {
      return new PaymentToInvPlan.Node(BigDecimal.ZERO);
    }

    var allocated = BigDecimal.ZERO;
    
    for (final var allocation : allocations) {
      // detail 1 - + payment share against fund 
      final var node = visitPaymentToInvPlanAlloc(new PaymentToInvPlanAlloc.Expression(payment, moneyRequest, invPlan, allocation));
      
      allocated = allocated.add(node.getAllocatedAmount());
      
      newBlackBook.addBlackBookDetail(bbd -> bbd
          .type("PAYMENT_DETAIL")
          .subType("PAYMENT_ALLOCATED_AMOUNT")
          .amount(node.getAllocatedAmount())
          .paymentId(payment.getId())
          .targetId(allocation.getId())
          .body(JsonObject.mapFrom(node))
          .build());

    }
    return new PaymentToInvPlan.Node(allocated);
  }

  
  
  private PaymentToInvPlanAlloc.Node visitPaymentToInvPlanAlloc(PaymentToInvPlanAlloc.Expression exp) {
    final Payment payment = exp.getPayment();
    final InvPlan invPlan = exp.getInvPlan();
    
    final MoneyRequest moneyRequest = exp.getMoneyRequest(); 

    final InvPlanAlloc allocation = exp.getAllocation(); 
    
    
    // net amount that is going to be allocated
    final var paymentGrossAmount = moneyRequest.getRequestAmount();
    final var paymentKappaPaymentFeeAmount = paymentGrossAmount.multiply(KAPPA).setScale(2, RoundingMode.HALF_UP);
    final var paymentNetAmount = paymentGrossAmount.subtract(paymentKappaPaymentFeeAmount);
    
    // share from the net amount that is going to be used
    final var allocatedShare = allocation.getInvPlanAllocPercentage();
    final var allocatedAmount = paymentNetAmount.multiply(allocatedShare).setScale(2, RoundingMode.HALF_UP);

    
    // Find unit price for this allocation
    final var fundUnitPrice = ctx.getLedger().getUnitPrices().stream()
        .filter(up -> up.getExternalId().equals(allocation.getInvPlanAllocCode()))
        .findFirst()
        .map(up -> up.getUnitValue())
        .orElse(BigDecimal.ONE);
    
    final var fundUnitAmount = allocatedAmount.divide(fundUnitPrice, 6, RoundingMode.HALF_UP);
    
    
    // FEEMI_SAV_001 formula calculation
    final var allocationGammaMortalityFee = allocatedAmount.multiply(GAMMA).setScale(2, RoundingMode.HALF_UP);
    final var allocationNetAmount = allocatedAmount.subtract(allocationGammaMortalityFee);
    
    
    final var feeLog = String.format("Fee calculation: %s * %s = %s", paymentGrossAmount, KAPPA, paymentKappaPaymentFeeAmount);
    
    return PaymentToInvPlanAlloc.Node.builder()
        .logs(Arrays.asList(feeLog))
        
        .invPlanId(invPlan.getId())
        .paymentId(payment.getId())
        
        .paymentGrossAmount(paymentGrossAmount)
        .paymentKappaPaymentFeeAmount(paymentKappaPaymentFeeAmount)
        .paymentNetAmount(paymentNetAmount)
        
        .allocatedShare(allocatedShare)
        .allocatedAmount(allocatedAmount)
        
        .fundUnitPrice(fundUnitPrice)
        .fundUnitAmount(fundUnitAmount)
        
        .allocationGammaMortalityFee(allocationGammaMortalityFee)
        .allocationNetAmount(allocationNetAmount)
        
        .build();
  }

}