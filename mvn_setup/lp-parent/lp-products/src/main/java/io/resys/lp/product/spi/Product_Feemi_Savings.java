package io.resys.lp.product.spi;

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

import java.math.BigDecimal;

import io.resys.thena.product.client.api.ImmutableProduct;
import io.resys.thena.product.client.api.Product;
import io.resys.thena.product.client.api.ProductRule;
import io.resys.thena.product.client.api.RuleMeta;
import io.resys.thena.product.client.api.RuleSubType;
import io.resys.thena.product.client.api.RuleType;

public class Product_Feemi_Savings {
  
  public static Product create() {
    return ImmutableProduct.builder()
        .productCode("FEEMI_SAV_001")
        .productName("Säästö- ja sijoitusvakuutus")
        .category("Investment/Savings Insurance")
        .target("Individual wealth accumulation with tax benefits")
        .description(ProductSamples.loadResourceText("feemi-savings-insurance.txt"))
        
        // ELIGIBILITY RULES
        .addRules(ProductRule.of(
            RuleType.ELIGIBILITY, RuleSubType.AGE, "ELIGIBILITY_AGE_INCEPTION",
            "Age Range: 18 - 75 years at inception",
            RuleMeta.builder().startAge(18).endAge(75).build()))
            
        .addRules(ProductRule.of(
            RuleType.ELIGIBILITY, RuleSubType.AGE, "ELIGIBILITY_AGE_MATURITY",
            "Maximum age 85 at maturity",
            RuleMeta.builder().endAge(85).putAttributes("type", "maturity").build()))
            
        .addRules(ProductRule.of(
            RuleType.ELIGIBILITY, RuleSubType.RESIDENCY, "ELIGIBILITY_RESIDENCY_FINNISH",
            "Finnish tax resident required",
            RuleMeta.builder().putAttributes("required", "finnish_tax_resident").build()))
            
        .addRules(ProductRule.of(
            RuleType.ELIGIBILITY, RuleSubType.HEALTH, "ELIGIBILITY_HEALTH_NONE",
            "No medical underwriting required",
            RuleMeta.builder().putAttributes("underwriting", "none").build()))
            
        // CONTRIBUTION RULES
        .addRules(ProductRule.of(
            RuleType.CONTRIBUTION, RuleSubType.AMOUNT, "CONTRIBUTION_INITIAL_RANGE",
            "Initial Payment: EUR 1,000 - EUR 50,000",
            RuleMeta.builder()
                .minAmount(new BigDecimal("1000"))
                .maxAmount(new BigDecimal("50000"))
                .putAttributes("type", "initial")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CONTRIBUTION, RuleSubType.AMOUNT, "CONTRIBUTION_REGULAR_MONTHLY",
            "Regular Payments: EUR 50 - EUR 5,000 per month",
            RuleMeta.builder()
                .minAmount(new BigDecimal("50"))
                .maxAmount(new BigDecimal("5000"))
                .putAttributes("frequency", "monthly")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CONTRIBUTION, RuleSubType.MONETARY, "CONTRIBUTION_ANNUAL_MAX",
            "Max Annual Total: EUR 60,000 (regulatory limit)",
            RuleMeta.builder()
                .maxAmount(new BigDecimal("60000"))
                .putAttributes("scope", "annual")
                .putAttributes("type", "regulatory_limit")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CONTRIBUTION, RuleSubType.MONETARY, "CONTRIBUTION_CONTRACT_MIN",
            "Min Contract Value: EUR 1,000 (below this = forced closure)",
            RuleMeta.builder()
                .minAmount(new BigDecimal("1000"))
                .putAttributes("scope", "contract_value")
                .putAttributes("consequence", "forced_closure")
                .build()))
                
        // FEE RULES
        .addRules(ProductRule.of(
            RuleType.FEE, RuleSubType.SETUP, "FEE_SETUP_PROMOTIONAL",
            "Setup Fee: EUR 0 (promotional, normally EUR 50)",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0"))
                .maxAmount(new BigDecimal("50"))
                .putAttributes("status", "promotional")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.FEE, RuleSubType.ANNUAL, "FEE_ANNUAL_POLICY",
            "Annual Policy Fee: EUR 36 (EUR 3 per month)",
            RuleMeta.builder()
                .minAmount(new BigDecimal("36"))
                .putAttributes("frequency", "annual")
                .putAttributes("monthly_equivalent", "3")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.FEE, RuleSubType.MANAGEMENT, "FEE_MANAGEMENT_BY_FUND",
            "Management Fee: 0.6% - 1.5% depending on fund choice",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0.006"))
                .maxAmount(new BigDecimal("0.015"))
                .putAttributes("basis", "fund_dependent")
                .putAttributes("frequency", "annual")
                .build()))
                
        // INVESTMENT RULES
        .addRules(ProductRule.of(
            RuleType.INVESTMENT, RuleSubType.OPTIONS, "INVESTMENT_GUARANTEED",
            "Guaranteed Option: 0.5% guaranteed annually, 0.8% fee",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0.005"))
                .putAttributes("type", "guaranteed")
                .putAttributes("fee", "0.008")
                .putAttributes("risk", "none")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.INVESTMENT, RuleSubType.OPTIONS, "INVESTMENT_BALANCED",
            "Balanced Fund: 60% bonds, 40% equity, 4-6% expected return, 1.2% fee",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0.04"))
                .maxAmount(new BigDecimal("0.06"))
                .putAttributes("type", "balanced")
                .putAttributes("asset_mix", "60_bonds_40_equity")
                .putAttributes("fee", "0.012")
                .putAttributes("risk", "medium")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.INVESTMENT, RuleSubType.ALLOCATION, "INVESTMENT_ALLOCATION_MIN",
            "Minimum 10% per fund if selected",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0.10"))
                .putAttributes("rule", "minimum_per_fund")
                .build()))
                
        // CLAIM RULES
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.WITHDRAWAL, "CLAIM_PARTIAL_MINIMUM",
            "Partial Withdrawal: Minimum EUR 500, must leave EUR 1,000 minimum",
            RuleMeta.builder()
                .minAmount(new BigDecimal("500"))
                .putAttributes("remaining_minimum", "1000")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.DEATH_BENEFIT, "CLAIM_DEATH_BENEFIT",
            "Death Benefit: Account value + EUR 1,000 death benefit",
            RuleMeta.builder()
                .minAmount(new BigDecimal("1000"))
                .putAttributes("type", "fixed_plus_account")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.TAX_TREATMENT, "CLAIM_TAX_CAPITAL_GAINS",
            "Capital gains tax (30%) only on gains portion, not contributions",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0.30"))
                .putAttributes("type", "capital_gains")
                .putAttributes("scope", "gains_only")
                .build()))
                
        .build();
  }
}