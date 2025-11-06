package io.resys.thena.product.client.api;

import java.math.BigDecimal;
import java.math.RoundingMode;

/*-
 * #%L
 * thena-product-client
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableProduct.class)
@JsonDeserialize(as = ImmutableProduct.class)
public interface Product {
  
  String getProductCode();
  String getProductName();
  String getCategory();
  String getTarget();
  String getDescription();
  List<ProductRule> getRules();
  
  
  Optional<IncomeRange> getIncomeRange();
  
  default Optional<AgeRange> getAgeRange() {
    // Look for age eligibility rules
    Optional<ProductRule> ageRule = this.getRuleByCode(ProductRuleCodes.CODE_ELIGIBILITY_001_AGE_INCEPTION);
    if (ageRule.isPresent() && ageRule.get().getMeta().getStartAge().isPresent() 
        && ageRule.get().getMeta().getEndAge().isPresent()) {
      return Optional.of(AgeRange.of(
        ageRule.get().getMeta().getStartAge().get(),
        ageRule.get().getMeta().getEndAge().get()
      ));
    }
    
    // Default fallback
    return Optional.empty();
  }
  
  default Optional<IncomeRange> getContributionRange() {
    // Look for regular payment amount rules
    Optional<ProductRule> paymentRule = this.getRuleByCode(ProductRuleCodes.CODE_CONTRIBUTION_002_REGULAR_MONTHLY);
    if (paymentRule.isPresent() && paymentRule.get().getMeta().getMinAmount().isPresent() 
        && paymentRule.get().getMeta().getMaxAmount().isPresent()) {
      
      BigDecimal minMonthly = paymentRule.get().getMeta().getMinAmount().get();
      BigDecimal maxMonthly = paymentRule.get().getMeta().getMaxAmount().get();
      
      // Convert to annual income range (assuming 3-8% contribution rate)
      final int minIncomeAnnual = minMonthly.multiply(new BigDecimal("12")).divide(new BigDecimal("0.08"), 0, RoundingMode.UP).intValue();
      final int maxIncomeAnnual = maxMonthly.multiply(new BigDecimal("12")).divide(new BigDecimal("0.03"), 0, RoundingMode.DOWN).intValue();
      
      return Optional.of(IncomeRange.of(minIncomeAnnual, maxIncomeAnnual));
    }
    
    // Default fallback
    // IncomeRange.of(25000, 100000)
    return Optional.empty();
  }
  
  default List<InvestmentOption> extractInvestmentOptions() {
    List<ProductRule> investmentRules = this.getRulesBySubType(RuleSubType.OPTIONS)
      .stream()
      .filter(rule -> rule.getType() == RuleType.INVESTMENT)
      .collect(Collectors.toList());
  
    return investmentRules.stream()
      .map(rule -> {
        final var code = extractCodeFromText(rule.getText());
        final var name = extractNameFromText(rule.getText());
        final var riskLevel = rule.getMeta().getAttributes().getOrDefault("risk", "MEDIUM");
        final var fee = new BigDecimal(rule.getMeta().getAttributes().getOrDefault("fee", "0.01"));
        
        return ImmutableInvestmentOption.builder()
            .code(code)
            .name(name)
            .riskLevel(riskLevel)
            .managementFee(fee)
            .build(); 
      })
      .collect(Collectors.toList());
  }
  
  default AllocationRules extractAllocationRules() {
    final Optional<ProductRule> allocationRule = this.getRuleByCode(ProductRuleCodes.CODE_INVESTMENT_013_ALLOCATION_MIN);
    
    BigDecimal minPercentage = BigDecimal.ZERO;
    if (allocationRule.isPresent() && allocationRule.get().getMeta().getMinAmount().isPresent()) {
      minPercentage = allocationRule.get().getMeta().getMinAmount().get();
    }
    return ImmutableAllocationRules.builder()
        .minimumPercentage(minPercentage)
        .maxFundsAllowed(100)
        .requiresTotal100(true)
        .build();
  }
  
  default List<CoverOption> getCoverOptions() {
    List<ProductRule> coverRules = this.getRulesByTypeAndSubType(RuleType.CLAIM, RuleSubType.DEATH_BENEFIT);
    
    return coverRules.stream()
        .map(rule -> {
          final var code = rule.getRuleCode();
          final var name = extractNameFromText(rule.getText());
          final var description = rule.getText();
          final var coverType = extractCoverTypeFromCode(code);
          final var amount = rule.getMeta().getMinAmount().orElse(BigDecimal.ZERO);
          final var taxTreatment = rule.getMeta().getAttributes().getOrDefault("tax_treatment", "STANDARD");
          final var beneficiaryType = rule.getMeta().getAttributes().getOrDefault("beneficiary_type", "ANY");
          
          return ImmutableCoverOption.builder()
              .code(code)
              .name(name)
              .description(description)
              .coverType(coverType)
              .amount(amount)
              .taxTreatment(taxTreatment)
              .beneficiaryType(beneficiaryType)
              .build();
        })
        .collect(Collectors.toList());
  }
  
  
  default List<ProductRule> getRulesByType(RuleType type) {
    return getRules().stream()
        .filter(rule -> rule.getType() == type)
        .collect(Collectors.toList());
  }
  
  default List<ProductRule> getRulesBySubType(RuleSubType subType) {
    return getRules().stream()
        .filter(rule -> rule.getSubType() == subType)
        .collect(Collectors.toList());
  }
  
  default List<ProductRule> getRulesByTypeAndSubType(RuleType type, RuleSubType subType) {
    return getRules().stream()
        .filter(rule -> rule.getType() == type && rule.getSubType() == subType)
        .collect(Collectors.toList());
  }
  
  default Optional<ProductRule> getRuleByCode(String ruleCode) {
    return getRules().stream()
        .filter(rule -> rule.getRuleCode().equals(ruleCode))
        .findFirst();
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
  
  private static String extractCoverTypeFromCode(String code) {
    // Extract cover type from rule code
    if (code.contains("DEATH_BENEFIT")) return "DEATH_BENEFIT";
    if (code.contains("INHERITANCE")) return "INHERITANCE_COVER";
    if (code.contains("BENEFICIARY")) return "BENEFICIARY_CLAUSE";
    return "BASIC_COVER";
  }
  
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableAllocationRules.class)
  @JsonDeserialize(as = ImmutableAllocationRules.class)
  interface AllocationRules {
    BigDecimal getMinimumPercentage();
    int getMaxFundsAllowed();
    boolean getRequiresTotal100();

  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableInvestmentOption.class)
  @JsonDeserialize(as = ImmutableInvestmentOption.class)
  interface InvestmentOption {
    String getCode();
    String getName();
    String getRiskLevel();
    BigDecimal getManagementFee();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableAgeRange.class)
  @JsonDeserialize(as = ImmutableAgeRange.class)
  interface AgeRange {
    int getMinAge();
    int getMaxAge();
    
    static AgeRange of(int minAge, int maxAge) {
      return ImmutableAgeRange.builder()
          .minAge(minAge)
          .maxAge(maxAge)
          .build();
    }
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableIncomeRange.class)
  @JsonDeserialize(as = ImmutableIncomeRange.class)
  interface IncomeRange {
    int getMinIncome();
    int getMaxIncome();
    
    static IncomeRange of(int minIncome, int maxIncome) {
      return ImmutableIncomeRange.builder()
          .minIncome(minIncome)
          .maxIncome(maxIncome)
          .build();
    }
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableCoverOption.class)
  @JsonDeserialize(as = ImmutableCoverOption.class)
  interface CoverOption {
    String getCode();
    String getName();
    String getDescription();
    String getCoverType();
    BigDecimal getAmount();
    String getTaxTreatment();
    String getBeneficiaryType();
  }
}