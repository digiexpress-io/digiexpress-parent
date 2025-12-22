package io.resys.lp.client.spi.formula.feemi_savings.monthly;

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
import java.util.Collections;
import java.util.List;

import io.resys.lp.client.api.LpClient.CalculationFormula;
import io.resys.lp.client.api.LpClient.FormulaContainer;
import io.resys.lp.client.api.entities.AnyCalculation;
import io.resys.lp.client.api.entities.Envelope;
import io.resys.lp.client.api.entities.Envelope.EnvelopeStatus;
import io.resys.lp.client.api.entities.ImmutableEnvelope;
import io.resys.lp.client.api.entities.ImmutableLog;
import io.resys.lp.client.spi.formula.feemi_savings.BlackBookConstants;
import io.resys.lp.client.spi.formula.feemi_savings.monthly.ast.MonthlyInvPlanAllocGrowth;
import io.resys.lp.client.spi.formula.feemi_savings.monthly.ast.MonthlyInvPlanGrowth;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.contract.client.entities.InvPlan;
import io.resys.thena.contract.client.entities.InvPlanAlloc;
import io.resys.thena.ledger.client.api.ThenaLedgerMergeObject.MergeLedger;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewBlackBook;
import io.resys.thena.ledger.client.entities.BlackBook;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Monthly_FeemiSavings implements CalculationFormula {

  private FormulaContainer ctx;
  private MergeLedger ledger;
  private NewBlackBook newBlackBook;
  private BlackBook lastBlackBook;

  // FEEMI_SAV_001 product parameters for monthly calculations
  private static final BigDecimal MU = new BigDecimal("0.05"); // 5% estimated annual return rate
  private static final BigDecimal MONTHLY_RETURN_RATE = MU.divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP); // Monthly rate
  private static final BigDecimal GAMMA = new BigDecimal("0.001"); // mortality rate
  private static final BigDecimal MONTHLY_GAMMA = GAMMA.divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP); // Monthly mortality fee

  
  public Uni<Envelope<AnyCalculation>> accept(FormulaContainer container) {
    this.ctx = container;
    
    final var currentBlackBookId = container.getLedger().getLedger().getCurrentBlackBookId().get();
    this.lastBlackBook =  container.getLedger().getBlackBooks().stream()
      .filter(e -> e.getId().equals(currentBlackBookId))
      .findFirst().get();
    

    final var missingDates = getMissingDates(container);
    
    if (missingDates.isEmpty()) {
      // Not time for monthly calculation yet
      return Uni.createFrom().item(ImmutableEnvelope.<AnyCalculation>builder()
          .status(EnvelopeStatus.OK)
          .addLogs(ImmutableLog.builder()
              .targetId(ctx.getLedger().getLedger().getId())
              .text("Monthly calculation not needed yet")
              .build())
          .object(null)
          .build());
    }
    
    return ctx.getLedgerClient().withTenant().commit().modifyOneLedger()
      .commitMessage("+ monthly portfolio growth calculation")
      .commitAuthor(Monthly_FeemiSavings.class.getSimpleName())
      .ledgerId(ctx.getLedger().getLedger().getId())
      .modifyLedger(ledger -> {
        this.ledger = ledger;
        
        // Execute monthly calculations
        for(final var targetDate : missingDates) {
          visitMonthlyPortfolioGrowth(targetDate);
        }
        
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
              .text("Monthly portfolio calculation completed")
              .build())
          .object(null)
          .build());
  }
  
  private void visitMonthlyPortfolioGrowth(LocalDate endOfMonth) {
    ledger.addBlackBook(bb -> {
      this.newBlackBook = bb;

      // Calculate total portfolio growth across all investment plans
      var totalGrowth = BigDecimal.ZERO;
      var totalMortalityFees = BigDecimal.ZERO;
      var totalNetGrowth = BigDecimal.ZERO;
      
      for (final var invPlan : ctx.getContract().getInvPlans()) {
        final var node = visitMonthlyInvPlanGrowth(new MonthlyInvPlanGrowth.Expression(invPlan));
        totalGrowth = totalGrowth.add(node.getGrossGrowth());
        totalMortalityFees = totalMortalityFees.add(node.getMortalityFees());
        totalNetGrowth = totalNetGrowth.add(node.getNetGrowth());
      }
      
      final var newBalance = lastBlackBook.getBookAmount().add(totalNetGrowth);

      // Create BlackBook entry for monthly calculation
      this.newBlackBook
        .amount(newBalance)                    // New portfolio balance
        .deltaAmount(totalNetGrowth)           // Net growth for the month
        .inflowAmount(totalGrowth)             // Growth from investments
        .outflowAmount(totalMortalityFees)     // Monthly mortality fees
        .date(endOfMonth)
        .type(BlackBookConstants.TYPE_MONTHLY_CALCULATION)
        .build();
    });
  }
      
  private MonthlyInvPlanGrowth.Node visitMonthlyInvPlanGrowth(MonthlyInvPlanGrowth.Expression exp) {
    final InvPlan invPlan = exp.getInvPlan();
    
    // Get allocations for this investment plan
    final var allocations = ctx.getContract().getInvPlanAllocations().get(invPlan.getId());
    if (allocations == null || allocations.isEmpty()) {
      return new MonthlyInvPlanGrowth.Node(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    var totalGrossGrowth = BigDecimal.ZERO;
    var totalMortalityFees = BigDecimal.ZERO;
    var totalNetGrowth = BigDecimal.ZERO;
    
    for (final var allocation : allocations) {
      final var node = visitMonthlyInvPlanAllocGrowth(new MonthlyInvPlanAllocGrowth.Expression(invPlan, allocation));
      
      totalGrossGrowth = totalGrossGrowth.add(node.getGrossGrowthAmount());
      totalMortalityFees = totalMortalityFees.add(node.getMortalityFeeAmount());
      totalNetGrowth = totalNetGrowth.add(node.getNetGrowthAmount());
      
      // Create detailed BlackBook entry for allocation growth
      newBlackBook.addBlackBookDetail(bbd -> bbd
          .type(BlackBookConstants.DETAIL_TYPE_MONTHLY_DETAIL)
          .subType(BlackBookConstants.SUBTYPE_ALLOCATION_GROWTH)
          .amount(node.getNewAllocationValue())         // New allocation value
          .deltaAmount(node.getNetGrowthAmount())       // Net growth
          .inflowAmount(node.getGrossGrowthAmount())    // Gross growth
          .outflowAmount(node.getMortalityFeeAmount())  // Mortality fee
          .targetId(allocation.getId())
          .body(JsonObject.mapFrom(node))
          .build());
    }
    
    return new MonthlyInvPlanGrowth.Node(totalGrossGrowth, totalMortalityFees, totalNetGrowth);
  }

  
  private MonthlyInvPlanAllocGrowth.Node visitMonthlyInvPlanAllocGrowth(MonthlyInvPlanAllocGrowth.Expression exp) {
    final InvPlan invPlan = exp.getInvPlan();
    final InvPlanAlloc allocation = exp.getAllocation();
    
    // Get current allocation value from latest BlackBook entries
    final var currentAllocationValue = getCurrentAllocationValue(allocation.getId());
    
    // Get current fund unit price
    final var fund = ctx.getFundQuery().getOne(allocation.getInvPlanAllocCode(), ctx.getToday());
    final var currentUnitPrice = fund.getCalculationValue().getPriceValue();
    
    // Calculate monthly growth using estimated return rate
    final var grossGrowthAmount = currentAllocationValue.multiply(MONTHLY_RETURN_RATE).setScale(2, RoundingMode.HALF_UP);
    
    // Apply monthly mortality fee
    final var mortalityFeeAmount = currentAllocationValue.multiply(MONTHLY_GAMMA).setScale(2, RoundingMode.HALF_UP);
    
    // Net growth after fees
    final var netGrowthAmount = grossGrowthAmount.subtract(mortalityFeeAmount);
    
    // New allocation value
    final var newAllocationValue = currentAllocationValue.add(netGrowthAmount);
    
    // Create detailed logs
    final var logs = new ArrayList<String>();
    logs.add(String.format("Current Allocation Value: %s", currentAllocationValue));
    logs.add(String.format("Monthly Growth: μ/12 × current_value = %s × %s = %s", MONTHLY_RETURN_RATE, currentAllocationValue, grossGrowthAmount));
    logs.add(String.format("Monthly Mortality Fee: γ/12 × current_value = %s × %s = %s", MONTHLY_GAMMA, currentAllocationValue, mortalityFeeAmount));
    logs.add(String.format("Net Growth: gross_growth - mortality_fee = %s - %s = %s", grossGrowthAmount, mortalityFeeAmount, netGrowthAmount));
    logs.add(String.format("New Allocation Value: current_value + net_growth = %s + %s = %s", currentAllocationValue, netGrowthAmount, newAllocationValue));
    logs.add(String.format("Current Fund Unit Price: %s", currentUnitPrice));
    
    return MonthlyInvPlanAllocGrowth.Node.builder()
        .logs(logs)
        .invPlanId(invPlan.getId())
        .allocationId(allocation.getId())
        .currentAllocationValue(currentAllocationValue)
        .grossGrowthAmount(grossGrowthAmount)
        .mortalityFeeAmount(mortalityFeeAmount)
        .netGrowthAmount(netGrowthAmount)
        .newAllocationValue(newAllocationValue)
        .fundUnitPrice(currentUnitPrice)
        .build();
  }
  
  private BigDecimal getCurrentAllocationValue(String allocationId) {
    // Use tree navigation to find allocation value since last monthly calculation
    final var tree = ctx.getLedger().toTree();
    
    // Get all nodes from current back to the last monthly calculation (or beginning)
    final var nodesSinceLastMonthly = tree.getTill(BlackBookConstants.TYPE_MONTHLY_CALCULATION);
    
    // Find the baseline value from the last monthly calculation (if any)
    BigDecimal baselineValue = BigDecimal.ZERO;
    final var lastMonthlyNode = nodesSinceLastMonthly.stream()
        .filter(node -> BlackBookConstants.TYPE_MONTHLY_CALCULATION.equals(node.getBlackBook().getBookType()))
        .findFirst();
        
    if (lastMonthlyNode.isPresent()) {
      // Get the allocation value from the last monthly calculation
      baselineValue = lastMonthlyNode.get().getBlackBookDetails().stream()
          .filter(detail -> allocationId.equals(detail.getTargetId().orElse(null)))
          .filter(detail -> BlackBookConstants.SUBTYPE_ALLOCATION_GROWTH.equals(detail.getDetailSubType().orElse(null)))
          .map(detail -> detail.getDetailAmount())
          .findFirst()
          .orElse(BigDecimal.ZERO);
    }
    
    // Sum all new payment allocations since the last monthly calculation
    final BigDecimal newAllocations = nodesSinceLastMonthly.stream()
        .filter(node -> !BlackBookConstants.TYPE_MONTHLY_CALCULATION.equals(node.getBlackBook().getBookType()))
        .flatMap(node -> node.getBlackBookDetails().stream())
        .filter(detail -> allocationId.equals(detail.getTargetId().orElse(null)))
        .filter(detail -> BlackBookConstants.SUBTYPE_PAYMENT_ALLOCATED_AMOUNT.equals(detail.getDetailSubType().orElse(null)))
        .map(detail -> detail.getDetailAmount())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    return baselineValue.add(newAllocations);
  }
  
  
  private List<LocalDate> getMissingDates(FormulaContainer container) {
    final var fromLastMonthly = container.getLedger().toTree()
      .getTill(BlackBookConstants.TYPE_MONTHLY_CALCULATION);
    
    // nothing to calculate from nothing
    if(fromLastMonthly.isEmpty()) {
      return Collections.emptyList();
    }
    
    final var lastMonthly = fromLastMonthly.stream()
      .filter(e -> BlackBookConstants.TYPE_MONTHLY_CALCULATION.equals(e.getBlackBook().getBookType()))
      .findFirst();
    
    final var isAfter = lastMonthly.get().getBlackBook().getBookDate().compareTo(container.getToday()) >= 0;
    if(lastMonthly.isPresent() && isAfter) {
      return Collections.emptyList();
    }
    
    final LocalDate startingDate;
    if(lastMonthly.isPresent()) {
      startingDate = lastMonthly.get().getBlackBook().getBookDate().withDayOfMonth(1).plusMonths(1);
    } else {
      startingDate = fromLastMonthly.stream()
        .filter(e -> e.getBlackBook().getBookAmount().compareTo(BigDecimal.ZERO) != 0)
        .sorted((a, b) -> a.getBlackBook().getBookDate().compareTo(b.getBlackBook().getBookDate()))
        .findFirst().map(e -> e.getBlackBook().getBookDate().withDayOfMonth(1))
        .orElse(null);
    }
    
    if(startingDate == null) {
      return Collections.emptyList();
    }
    
    final var missingDates = new ArrayList<LocalDate>();
    var nextDate = startingDate;
    do {
      final var endOfMonth = nextDate.withDayOfMonth(1).plusMonths(1).minusDays(1);
      missingDates.add(endOfMonth);
      nextDate = endOfMonth.plusDays(1);
    } while(container.getToday().compareTo(nextDate) >= 0);
        
    return missingDates;
  } 
}