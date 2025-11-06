package io.resys.thena.contract.samples.products;

/*-
 * #%L
 * thena-contract-client
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.resys.thena.contract.samples.GenerationOptions.AgeRange;
import io.resys.thena.contract.samples.GenerationOptions.IncomeRange;
import io.resys.thena.product.client.api.Product;
import io.resys.thena.product.client.api.ProductRule;
import io.resys.thena.product.client.api.RuleSubType;
import io.resys.thena.product.client.api.RuleType;



public class ProductConstraintExtractor {
  
  public static class ProductConstraints {
    public final AgeRange ageRange;
    public final IncomeRange contributionRange;
    public final List<InvestmentOption> investmentOptions;
    public final AllocationRules allocationRules;
    
    public ProductConstraints(AgeRange ageRange, IncomeRange contributionRange, 
                             List<InvestmentOption> investmentOptions, AllocationRules allocationRules) {
      this.ageRange = ageRange;
      this.contributionRange = contributionRange;
      this.investmentOptions = investmentOptions;
      this.allocationRules = allocationRules;
    }
  }
  
  public static class InvestmentOption {
    public final String code;
    public final String name;
    public final String riskLevel;
    public final BigDecimal managementFee;
    
    public InvestmentOption(String code, String name, String riskLevel, BigDecimal managementFee) {
      this.code = code;
      this.name = name;
      this.riskLevel = riskLevel;
      this.managementFee = managementFee;
    }
  }
  
  public static class AllocationRules {
    public final BigDecimal minimumPercentage;
    public final int maxFundsAllowed;
    public final boolean requiresTotal100;
    
    public AllocationRules(BigDecimal minimumPercentage, int maxFundsAllowed, boolean requiresTotal100) {
      this.minimumPercentage = minimumPercentage;
      this.maxFundsAllowed = maxFundsAllowed;
      this.requiresTotal100 = requiresTotal100;
    }
  }
  
  public static ProductConstraints extractConstraints(Product product) {
    return new ProductConstraints(
      extractAgeRange(product),
      extractContributionRange(product),
      extractInvestmentOptions(product),
      extractAllocationRules(product)
    );
  }
  
  private static AgeRange extractAgeRange(Product product) {
    // Look for age eligibility rules
    Optional<ProductRule> ageRule = product.getRuleByCode("ELIGIBILITY_AGE_INCEPTION");
    if (ageRule.isPresent() && ageRule.get().getMeta().getStartAge().isPresent() 
        && ageRule.get().getMeta().getEndAge().isPresent()) {
      return AgeRange.of(
        ageRule.get().getMeta().getStartAge().get(),
        ageRule.get().getMeta().getEndAge().get()
      );
    }
    
    // Default fallback
    return AgeRange.of(18, 70);
  }
  
  private static IncomeRange extractContributionRange(Product product) {
    // Look for regular payment amount rules
    Optional<ProductRule> paymentRule = product.getRuleByCode("CONTRIBUTION_REGULAR_MONTHLY");
    if (paymentRule.isPresent() && paymentRule.get().getMeta().getMinAmount().isPresent() 
        && paymentRule.get().getMeta().getMaxAmount().isPresent()) {
      
      BigDecimal minMonthly = paymentRule.get().getMeta().getMinAmount().get();
      BigDecimal maxMonthly = paymentRule.get().getMeta().getMaxAmount().get();
      
      // Convert to annual income range (assuming 3-8% contribution rate)
      int minIncomeAnnual = minMonthly.multiply(new BigDecimal("12")).divide(new BigDecimal("0.08"), 0, BigDecimal.ROUND_UP).intValue();
      int maxIncomeAnnual = maxMonthly.multiply(new BigDecimal("12")).divide(new BigDecimal("0.03"), 0, BigDecimal.ROUND_DOWN).intValue();
      
      return IncomeRange.of(minIncomeAnnual, maxIncomeAnnual);
    }
    
    // Default fallback
    return IncomeRange.of(25000, 100000);
  }
  
  private static List<InvestmentOption> extractInvestmentOptions(Product product) {
    List<ProductRule> investmentRules = product.getRulesBySubType(RuleSubType.OPTIONS)
        .stream()
        .filter(rule -> rule.getType() == RuleType.INVESTMENT)
        .collect(Collectors.toList());
    
    return investmentRules.stream()
        .map(rule -> {
          String code = extractCodeFromText(rule.getText());
          String name = extractNameFromText(rule.getText());
          String riskLevel = rule.getMeta().getAttributes().getOrDefault("risk", "MEDIUM");
          BigDecimal fee = new BigDecimal(rule.getMeta().getAttributes().getOrDefault("fee", "0.01"));
          
          return new InvestmentOption(code, name, riskLevel, fee);
        })
        .collect(Collectors.toList());
  }
  
  private static AllocationRules extractAllocationRules(Product product) {
    Optional<ProductRule> allocationRule = product.getRuleByCode("INVESTMENT_ALLOCATION_MIN");
    
    BigDecimal minPercentage = BigDecimal.ZERO;
    if (allocationRule.isPresent() && allocationRule.get().getMeta().getMinAmount().isPresent()) {
      minPercentage = allocationRule.get().getMeta().getMinAmount().get();
    }
    
    return new AllocationRules(minPercentage, 10, true);
  }
  
  private static String extractCodeFromText(String text) {
    // Extract investment code from rule text like "Guaranteed Option: ..."
    if (text.contains("Guaranteed")) return "TAATTU_TUOTTO";
    if (text.contains("Balanced")) return "TASAPAINOINEN_RAHASTO";
    if (text.contains("Equity")) return "OSAKERAHASTO";
    if (text.contains("Index")) return "INDEKSIRAHASTO";
    return "UNKNOWN";
  }
  
  private static String extractNameFromText(String text) {
    // Extract readable name from rule text
    if (text.contains(":")) {
      return text.substring(0, text.indexOf(":")).trim();
    }
    return text.trim();
  }
}