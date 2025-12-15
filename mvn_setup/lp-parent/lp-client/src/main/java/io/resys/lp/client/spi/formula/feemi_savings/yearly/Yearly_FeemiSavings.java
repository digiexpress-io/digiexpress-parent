package io.resys.lp.client.spi.formula.feemi_savings.yearly;

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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import io.resys.lp.client.api.LpClient.CalculationFormula;
import io.resys.lp.client.api.LpClient.FormulaContainer;
import io.resys.lp.client.api.entities.AnyCalculation;
import io.resys.lp.client.api.entities.Envelope;
import io.resys.lp.client.api.entities.Envelope.EnvelopeStatus;
import io.resys.lp.client.api.entities.ImmutableEnvelope;
import io.resys.lp.client.api.entities.ImmutableLog;
import io.resys.lp.client.spi.formula.feemi_savings.BlackBookConstants;
import io.resys.lp.client.spi.formula.feemi_savings.yearly.ast.YearlyInvPlanAllocProcessing;
import io.resys.lp.client.spi.formula.feemi_savings.yearly.ast.YearlyInvPlanProcessing;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.contract.client.entities.InvPlan;
import io.resys.thena.contract.client.entities.InvPlanAlloc;
import io.resys.thena.ledger.client.api.ThenaLedgerMergeObject.MergeLedger;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewBlackBook;
import io.resys.thena.ledger.client.entities.BlackBook;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Yearly_FeemiSavings implements CalculationFormula {

  private FormulaContainer ctx;
  private MergeLedger ledger;
  private NewBlackBook newBlackBook;
  private BlackBook lastBlackBook;

  // FEEMI_SAV_001 product parameters for yearly calculations
  private static final BigDecimal ANNUAL_POLICY_FEE = new BigDecimal("36");     // EUR 36 annual policy fee
  
  
  public Uni<Envelope<AnyCalculation>> accept(FormulaContainer container) {
    this.ctx = container;
    
    // Get sorted BlackBooks to find the last entry
    final var sortedBb = new ArrayList<>(ctx.getLedger().getBlackBooks());
    sortedBb.sort(BlackBook.COMPARATOR);
    this.lastBlackBook = sortedBb.getFirst();
    
    // Check if yearly calculation is needed (has it been a year since last calculation?)
    final var lastCalculationDate = findLastYearlyCalculationDate();
    final var daysSinceLastYearlyCalculation = ChronoUnit.DAYS.between(lastCalculationDate, ctx.getToday());
    
    if (daysSinceLastYearlyCalculation < 365) {
      // Not time for yearly calculation yet
      return Uni.createFrom().item(ImmutableEnvelope.<AnyCalculation>builder()
          .status(EnvelopeStatus.OK)
          .addLogs(ImmutableLog.builder()
              .targetId(ctx.getLedger().getLedger().getId())
              .text("Yearly calculation not needed yet (last yearly calculation: " + lastCalculationDate + ")")
              .build())
          .object(null)
          .build());
    }
    
    return ctx.getLedgerClient().withTenant().commit().modifyOneLedger()
      .commitMessage("+ yearly portfolio processing")
      .commitAuthor(Yearly_FeemiSavings.class.getSimpleName())
      .ledgerId(ctx.getLedger().getLedger().getId())
      .modifyLedger(ledger -> {
        this.ledger = ledger;
        
        // Execute yearly calculations
        visitYearlyPortfolioProcessing();
        
        this.ledger.build();
      }).build()
      
      // Map to result
      .onItem().transform(env -> ImmutableEnvelope.<AnyCalculation>builder()
          .status(env.getStatus() == CommitResultStatus.OK ? EnvelopeStatus.OK : EnvelopeStatus.ERROR)
          .addAllLogs(env.getMessages().stream().map(e -> ImmutableLog.builder()
              .exception(e.getException())
              .text(e.getText())
              .build()).toList())
          .addLogs(ImmutableLog.builder()
              .targetId(ctx.getLedger().getLedger().getId())
              .text("Yearly portfolio processing completed")
              .build())
          .object(null)
          .build());
  }
  
  private void visitYearlyPortfolioProcessing() {
    ledger.addBlackBook(bb -> {
      this.newBlackBook = bb;

      // Portfolio Totals (Formula 5): Aggregate all fund subscription values
      var totalPolicyFees = BigDecimal.ZERO;
      var totalPerformance = BigDecimal.ZERO;
      var totalNetChange = BigDecimal.ZERO;
      
      for (final var invPlan : ctx.getContract().getInvPlans()) {
        final var node = visitYearlyInvPlanProcessing(new YearlyInvPlanProcessing.Expression(invPlan));
        totalPolicyFees = totalPolicyFees.add(node.getPlanPolicyFees());
        totalPerformance = totalPerformance.add(node.getPlanPerformance());
        totalNetChange = totalNetChange.add(node.getNetPlanChange());
      }
      
      // New Portfolio Balance (Formula 6): Previous Balance + Total Net Change
      final var newBalance = lastBlackBook.getBookAmount().add(totalNetChange);

      // Create BlackBook entry for yearly calculation
      this.newBlackBook
        .amount(newBalance)                        // New portfolio balance after yearly processing
        .deltaAmount(totalNetChange)               // Net change for the year (performance - fees)
        .inflowAmount(totalPerformance)            // Performance gains
        .outflowAmount(totalPolicyFees)            // Annual policy fees
        .date(ctx.getToday())
        .type(BlackBookConstants.TYPE_YEARLY_CALCULATION)
        .build();
    });
  }
      
  private YearlyInvPlanProcessing.Node visitYearlyInvPlanProcessing(YearlyInvPlanProcessing.Expression exp) {
    final InvPlan invPlan = exp.getInvPlan();
    
    // Get allocations for this investment plan
    final var allocations = ctx.getContract().getInvPlanAllocations().get(invPlan.getId());
    if (allocations == null || allocations.isEmpty()) {
      return new YearlyInvPlanProcessing.Node(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    var totalPolicyFees = BigDecimal.ZERO;
    var totalPerformance = BigDecimal.ZERO;
    var totalNetChange = BigDecimal.ZERO;
    
    for (final var allocation : allocations) {
      final var node = visitYearlyInvPlanAllocProcessing(new YearlyInvPlanAllocProcessing.Expression(invPlan, allocation));
      
      totalPolicyFees = totalPolicyFees.add(node.getPolicyFeePerSubscription());
      totalPerformance = totalPerformance.add(node.getYearlyPerformance());
      totalNetChange = totalNetChange.add(node.getNetYearlyChange());
      
      // Create detailed BlackBook entries for yearly fund subscription processing
      newBlackBook.addBlackBookDetail(bbd -> bbd
          .type(BlackBookConstants.DETAIL_TYPE_YEARLY_DETAIL)
          .subType(BlackBookConstants.SUBTYPE_ALLOCATION_PERFORMANCE)
          .amount(node.getCurrentSubscriptionValue())     // Current subscription value
          .deltaAmount(node.getNetYearlyChange())         // Net yearly change after fees
          .inflowAmount(node.getYearlyPerformance())      // Performance gains
          .outflowAmount(node.getPolicyFeePerSubscription()) // Policy fee allocation
          .targetId(allocation.getId())
          .body(JsonObject.mapFrom(node))
          .build());
    }
    
    return new YearlyInvPlanProcessing.Node(totalPolicyFees, totalPerformance, totalNetChange);
  }

  
  private YearlyInvPlanAllocProcessing.Node visitYearlyInvPlanAllocProcessing(YearlyInvPlanAllocProcessing.Expression exp) {
    final InvPlan invPlan = exp.getInvPlan();
    final InvPlanAlloc allocation = exp.getAllocation();
    
    // Get starting and current subscription values
    final var startingValue = getFundSubscriptionValueAtYearStart(allocation.getId());
    final var currentValue = getCurrentFundSubscriptionValue(allocation.getId());
    
    // Yearly Performance Calculation (Formula 2): Current - Starting Subscription Value
    final var yearlyPerformance = currentValue.subtract(startingValue);
    
    // Annual Policy Fee Distribution (Formula 1): (Fund Subscription Value / Total Portfolio Value) × EUR 36
    final var totalPortfolioValue = getTotalPortfolioValue();
    if (totalPortfolioValue.compareTo(BigDecimal.ZERO) == 0) {
      throw new IllegalStateException("Total portfolio value cannot be zero for yearly calculation");
    }
    final var subscriptionWeight = currentValue.divide(totalPortfolioValue, 6, RoundingMode.HALF_UP);
    final var policyFeePerSubscription = ANNUAL_POLICY_FEE.multiply(subscriptionWeight).setScale(2, RoundingMode.HALF_UP);
    
    // Net Yearly Change (Formula 4): Yearly Performance - Policy Fee per Fund Subscription
    final var netYearlyChange = yearlyPerformance.subtract(policyFeePerSubscription);
    
    // Fund Performance Tracking (Formula 3): (End Price - Start Price) / Start Price × 100
    final var lastYearDate = findLastYearlyCalculationDate();
    final var fundStart = ctx.getFundQuery().getOne(allocation.getInvPlanAllocCode(), lastYearDate);
    final var fundEnd = ctx.getFundQuery().getOne(allocation.getInvPlanAllocCode(), ctx.getToday());
    
    if (fundStart == null || fundEnd == null) {
      throw new IllegalStateException("Fund price data not available for allocation: " + allocation.getInvPlanAllocCode());
    }
    
    final var fundStartPrice = fundStart.getCalculationValue().getPriceValue();
    final var fundEndPrice = fundEnd.getCalculationValue().getPriceValue();
    
    if (fundStartPrice.compareTo(BigDecimal.ZERO) == 0) {
      throw new IllegalStateException("Fund start price cannot be zero for performance calculation");
    }
    
    final var fundPerformance = fundEndPrice.subtract(fundStartPrice).divide(fundStartPrice, 6, RoundingMode.HALF_UP);
    
    // Create detailed logs with formula references
    final var logs = new ArrayList<String>();
    logs.add(String.format("Starting Subscription Value: %s", startingValue));
    logs.add(String.format("Current Subscription Value: %s", currentValue));
    logs.add(String.format("Yearly Performance (Formula 2): current - starting = %s - %s = %s", currentValue, startingValue, yearlyPerformance));
    logs.add(String.format("Subscription Weight: subscription_value / portfolio_value = %s / %s = %s", currentValue, totalPortfolioValue, subscriptionWeight));
    logs.add(String.format("Policy Fee per Subscription (Formula 1): weight × EUR_36 = %s × %s = %s", subscriptionWeight, ANNUAL_POLICY_FEE, policyFeePerSubscription));
    logs.add(String.format("Fund Performance (Formula 3): (end_price - start_price) / start_price = (%s - %s) / %s = %s%%", fundEndPrice, fundStartPrice, fundStartPrice, fundPerformance.multiply(new BigDecimal("100"))));
    logs.add(String.format("Net Yearly Change (Formula 4): performance - policy_fee = %s - %s = %s", yearlyPerformance, policyFeePerSubscription, netYearlyChange));
    
    return YearlyInvPlanAllocProcessing.Node.builder()
        .logs(logs)
        .invPlanId(invPlan.getId())
        .allocationId(allocation.getId())
        .startingSubscriptionValue(startingValue)
        .currentSubscriptionValue(currentValue)
        .yearlyPerformance(yearlyPerformance)
        .policyFeePerSubscription(policyFeePerSubscription)
        .netYearlyChange(netYearlyChange)
        .fundStartPrice(fundStartPrice)
        .fundEndPrice(fundEndPrice)
        .fundPerformance(fundPerformance)
        .build();
  }
  
  private LocalDate findLastYearlyCalculationDate() {
    // Find the last yearly calculation, or use contract start date
    final var tree = ctx.getLedger().toTree();
    final var lastYearlyNode = tree.getTill(BlackBookConstants.TYPE_YEARLY_CALCULATION).stream()
        .filter(node -> BlackBookConstants.TYPE_YEARLY_CALCULATION.equals(node.getBlackBook().getBookType()))
        .findFirst();
        
    if (lastYearlyNode.isPresent()) {
      return lastYearlyNode.get().getBlackBook().getBookDate();
    }
    
    // If no yearly calculation found, use contract issue date
    return ctx.getContract().getContract().getContractIssueDate();
  }
  
  private BigDecimal getFundSubscriptionValueAtYearStart(String allocationId) {
    final var lastYearlyDate = findLastYearlyCalculationDate();
    
    // Use tree navigation to find subscription value at year start
    final var tree = ctx.getLedger().toTree();
    final var nodesSinceYearStart = tree.getTill(BlackBookConstants.TYPE_YEARLY_CALCULATION);
    
    // Find the baseline value from the last yearly calculation (if any)
    final var lastYearlyNode = nodesSinceYearStart.stream()
        .filter(node -> BlackBookConstants.TYPE_YEARLY_CALCULATION.equals(node.getBlackBook().getBookType()))
        .findFirst();
        
    if (lastYearlyNode.isPresent()) {
      // Get the subscription value from the last yearly calculation
      return lastYearlyNode.get().getBlackBookDetails().stream()
          .filter(detail -> allocationId.equals(detail.getTargetId().orElse(null)))
          .filter(detail -> BlackBookConstants.SUBTYPE_ALLOCATION_PERFORMANCE.equals(detail.getDetailSubType().orElse(null)))
          .map(detail -> detail.getDetailAmount())
          .findFirst()
          .orElse(BigDecimal.ZERO);
    }
    
    // First year scenario: Calculate the baseline from contract start
    // Sum all allocation values from contract start to last yearly calculation date
    final var allNodes = ctx.getLedger().toTree().getFrom(lastYearlyDate);
    return allNodes
        .flatMap(node -> node.getBlackBookDetails().stream())
        .filter(detail -> allocationId.equals(detail.getTargetId().orElse(null)))
        .filter(detail -> 
            BlackBookConstants.SUBTYPE_PAYMENT_ALLOCATED_AMOUNT.equals(detail.getDetailSubType().orElse(null)) ||
            BlackBookConstants.SUBTYPE_ALLOCATION_GROWTH.equals(detail.getDetailSubType().orElse(null)))
        .map(detail -> detail.getDetailDeltaAmount().orElse(BigDecimal.ZERO))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
  
  private BigDecimal getCurrentFundSubscriptionValue(String allocationId) {
    // Use tree navigation to find the most recent allocation value
    final var tree = ctx.getLedger().toTree();
    
    // Get all nodes from current back to the last yearly calculation (or beginning)
    final var nodesSinceLastYearly = tree.getTill(BlackBookConstants.TYPE_YEARLY_CALCULATION);
    
    // Find the baseline value from the last yearly calculation (if any)
    BigDecimal baselineValue = getFundSubscriptionValueAtYearStart(allocationId);
    
    // Sum all net changes (deltaAmount) since the last yearly calculation
    // This properly tracks the running balance changes for the allocation
    final BigDecimal yearlyChanges = nodesSinceLastYearly.stream()
        .filter(node -> !BlackBookConstants.TYPE_YEARLY_CALCULATION.equals(node.getBlackBook().getBookType()))
        .flatMap(node -> node.getBlackBookDetails().stream())
        .filter(detail -> allocationId.equals(detail.getTargetId().orElse(null)))
        .filter(detail -> 
            BlackBookConstants.SUBTYPE_PAYMENT_ALLOCATED_AMOUNT.equals(detail.getDetailSubType().orElse(null)) ||
            BlackBookConstants.SUBTYPE_ALLOCATION_GROWTH.equals(detail.getDetailSubType().orElse(null)))
        .map(detail -> detail.getDetailDeltaAmount().orElse(BigDecimal.ZERO))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    return baselineValue.add(yearlyChanges);
  }
  
  private BigDecimal getTotalPortfolioValue() {
    // Sum all current fund subscription values
    return ctx.getContract().getInvPlans().stream()
        .flatMap(plan -> ctx.getContract().getInvPlanAllocations().getOrDefault(plan.getId(), new ArrayList<>()).stream())
        .map(allocation -> getCurrentFundSubscriptionValue(allocation.getId()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}