package io.resys.lp.client.spi.formula.feemi_savings.addpayment;

/*-
 * #%L
 * lp-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import io.resys.lp.client.api.LpClient.CalculationFormula;
import io.resys.lp.client.api.LpClient.FormulaContainer;
import io.resys.lp.client.api.entities.AnyCalculation;
import io.resys.lp.client.api.entities.Envelope;
import io.resys.lp.client.api.entities.Envelope.EnvelopeStatus;
import io.resys.lp.client.spi.formula.feemi_savings.BlackBookConstants;
import io.resys.lp.client.spi.formula.feemi_savings.addpayment.ast.MoneyToLedger;
import io.resys.lp.client.spi.formula.feemi_savings.addpayment.ast.PaymentToInvPlan;
import io.resys.lp.client.spi.formula.feemi_savings.addpayment.ast.PaymentToInvPlanAlloc;
import io.resys.lp.client.api.entities.ImmutableEnvelope;
import io.resys.lp.client.api.entities.ImmutableLog;
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
public class AddPayment_FeemiSavings implements CalculationFormula {

  private FormulaContainer ctx;
  private MergeLedger ledger;
  private NewBlackBook newBlackBook;
  private BlackBook lastBlackBook;

  
  // FEEMI_SAV_001 product parameters
  private static final BigDecimal KAPPA = BigDecimal.ZERO; // 0% payment fee per spec
  private static final BigDecimal GAMMA = new BigDecimal("0.001"); // mortality rate from death cover
  private static final BigDecimal MU = new BigDecimal("0.05"); // estimated return rate

  
  public Uni<Envelope<AnyCalculation>> accept(FormulaContainer container) {
    this.ctx = container;
    
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

      // execute calculations
      var totalInflow = BigDecimal.ZERO;
      var totalOutflow = BigDecimal.ZERO;
      
      for (final var request : openRequests) {
        final var node = visitMoneyToLedger(new MoneyToLedger.Expression(request));
        totalInflow = totalInflow.add(node.getInflow());
        totalOutflow = totalOutflow.add(node.getOutflow());
      }
      
      final var netChange = totalInflow.subtract(totalOutflow);
      final var newBalance = lastBlackBook.getBookAmount().add(netChange);

      // end - create BlackBook entry with proper money tracking
      this.newBlackBook
        .amount(newBalance)              // Running balance
        .deltaAmount(netChange)          // Net change  
        .inflowAmount(totalInflow)       // Total money in
        .outflowAmount(totalOutflow)     // Total money out
        .date(LocalDate.now())
        .type(BlackBookConstants.TYPE_INCOMING_PAYMENT)
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
      return new MoneyToLedger.Node(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
    
    // Cross join with investment plans and sum flows
    var totalAllocated = BigDecimal.ZERO;
    var totalInflow = BigDecimal.ZERO;
    var totalOutflow = BigDecimal.ZERO;
    
    for (final var invPlan : ctx.getContract().getInvPlans()) {
      final var node = visitPaymentToInvPlan(new PaymentToInvPlan.Expression(payment, moneyRequest, invPlan));
      totalAllocated = totalAllocated.add(node.getAllocated());
      totalInflow = totalInflow.add(node.getInflow());
      totalOutflow = totalOutflow.add(node.getOutflow());
    }
    
    ledger.modifyMoneyRequest(moneyRequest.getId(), change -> {
      change.status(MoneyRequestStatus.CLOSED).build();
    });
    
    return new MoneyToLedger.Node(totalAllocated, totalInflow, totalOutflow);
  }
  
  
  
  private PaymentToInvPlan.Node visitPaymentToInvPlan(PaymentToInvPlan.Expression exp) {
    
    final Payment payment = exp.getPayment(); 
    final MoneyRequest moneyRequest = exp.getMoneyRequest(); 
    final InvPlan invPlan = exp.getInvPlan();
    
    // Get allocations for this investment plan
    final var allocations = ctx.getContract().getInvPlanAllocations().get(invPlan.getId());
    if (allocations == null || allocations.isEmpty()) {
      return new PaymentToInvPlan.Node(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    var totalAllocated = BigDecimal.ZERO;
    var totalInflow = BigDecimal.ZERO;
    var totalOutflow = BigDecimal.ZERO;
    
    for (final var allocation : allocations) {
      // detail 1 - + payment share against fund 
      final var node = visitPaymentToInvPlanAlloc(new PaymentToInvPlanAlloc.Expression(payment, moneyRequest, invPlan, allocation));
      
      totalAllocated = totalAllocated.add(node.getAllocatedAmount());
      
      // Calculate inflow and outflow for this allocation
      final var allocationInflow = node.getPaymentGrossAmount(); // Money coming in
      final var allocationOutflow = node.getPaymentKappaPaymentFeeAmount().add(node.getAllocationGammaMortalityFee()); // Fees going out
      
      totalInflow = totalInflow.add(allocationInflow);
      totalOutflow = totalOutflow.add(allocationOutflow);
      
      // Create detailed BlackBook entry with money tracking
      newBlackBook.addBlackBookDetail(bbd -> bbd
          .type(BlackBookConstants.DETAIL_TYPE_PAYMENT_DETAIL)
          .subType(BlackBookConstants.SUBTYPE_PAYMENT_ALLOCATED_AMOUNT)
          .amount(node.getAllocatedAmount())           // Final allocated amount
          .deltaAmount(node.getAllocationNetAmount())  // Net after fees
          .inflowAmount(allocationInflow)              // Gross payment
          .outflowAmount(allocationOutflow)            // Total fees
          .paymentId(payment.getId())
          .targetId(allocation.getId())
          .body(JsonObject.mapFrom(node))
          .build());
    }
    return new PaymentToInvPlan.Node(totalAllocated, totalInflow, totalOutflow);
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
    final var fund = ctx.getFundQuery().getOne(allocation.getInvPlanAllocCode(), payment.getPaymentDate());
    final var fundUnitPrice = fund.getCalculationValue().getPriceValue();
    
    final var fundUnitAmount = allocatedAmount.divide(fundUnitPrice, 6, RoundingMode.HALF_UP);
    
    
    // FEEMI_SAV_001 formula calculation
    final var allocationGammaMortalityFee = allocatedAmount.multiply(GAMMA).setScale(2, RoundingMode.HALF_UP);
    final var allocationNetAmount = allocatedAmount.subtract(allocationGammaMortalityFee);
    
    
    // Create detailed formula logs
    final var logs = new ArrayList<String>();
    logs.add(String.format("Payment Fee: κ × gross_amount = %s × %s = %s", KAPPA, paymentGrossAmount, paymentKappaPaymentFeeAmount));
    logs.add(String.format("Net Payment: gross_amount - payment_fee = %s - %s = %s", paymentGrossAmount, paymentKappaPaymentFeeAmount, paymentNetAmount));
    logs.add(String.format("Allocation: net_payment × allocation_share = %s × %s = %s", paymentNetAmount, allocatedShare, allocatedAmount));
    logs.add(String.format("Fund Units: allocated_amount ÷ unit_price = %s ÷ %s = %s", allocatedAmount, fundUnitPrice, fundUnitAmount));
    logs.add(String.format("Mortality Fee: γ × allocated_amount = %s × %s = %s", GAMMA, allocatedAmount, allocationGammaMortalityFee));
    logs.add(String.format("Net Allocation: allocated_amount - mortality_fee = %s - %s = %s", allocatedAmount, allocationGammaMortalityFee, allocationNetAmount));
    
    return PaymentToInvPlanAlloc.Node.builder()
        .logs(logs)
        
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
