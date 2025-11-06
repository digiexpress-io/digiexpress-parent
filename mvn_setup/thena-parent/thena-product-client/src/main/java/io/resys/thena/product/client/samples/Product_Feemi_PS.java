package io.resys.thena.product.client.samples;

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

import io.resys.thena.product.client.api.Product;
import io.resys.thena.product.client.api.ProductRule;
import io.resys.thena.product.client.api.RuleMeta;
import io.resys.thena.product.client.api.RuleSubType;
import io.resys.thena.product.client.api.RuleType;

public class Product_Feemi_PS {
  
  public static Product create() {
    return Product.builder()
        .productCode("FEEMI_PS_001")
        .productName("PS-vakuutus (Long-term Savings Insurance)")
        .category("Government Incentivized Long-term Savings")
        .target("Long-term wealth accumulation with government bonus")
        .description(ProductSamples.loadResourceText("feemi-ps-insurance.txt"))
        
        // ELIGIBILITY RULES
        .addRules(ProductRule.of(
            RuleType.ELIGIBILITY, RuleSubType.AGE, "ELIGIBILITY_AGE_INCEPTION",
            "Age Range: 18 - 62 years at inception",
            RuleMeta.builder().startAge(18).endAge(62).build()))
            
        .addRules(ProductRule.of(
            RuleType.ELIGIBILITY, RuleSubType.AGE, "ELIGIBILITY_AGE_CONVERSION",
            "Must convert before age 68",
            RuleMeta.builder()
                .endAge(68)
                .putAttributes("type", "conversion_deadline")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.ELIGIBILITY, RuleSubType.RESTRICTIONS, "ELIGIBILITY_ONE_PS_POLICY",
            "Cannot have active PS-vakuutus with another provider",
            RuleMeta.builder()
                .putAttributes("limit", "one_per_person_cross_provider")
                .build()))
                
        // CONTRIBUTION RULES
        .addRules(ProductRule.of(
            RuleType.CONTRIBUTION, RuleSubType.AMOUNT, "CONTRIBUTION_INITIAL_RANGE",
            "Initial Payment: EUR 50 - EUR 5,000",
            RuleMeta.builder()
                .minAmount(new BigDecimal("50"))
                .maxAmount(new BigDecimal("5000"))
                .putAttributes("type", "initial")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CONTRIBUTION, RuleSubType.AMOUNT, "CONTRIBUTION_REGULAR_MONTHLY",
            "Regular Payments: EUR 50 - EUR 416 per month",
            RuleMeta.builder()
                .minAmount(new BigDecimal("50"))
                .maxAmount(new BigDecimal("416"))
                .putAttributes("frequency", "monthly")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CONTRIBUTION, RuleSubType.MONETARY, "CONTRIBUTION_ANNUAL_LIMIT",
            "Annual Limits: EUR 5,000 maximum per year (government limit)",
            RuleMeta.builder()
                .maxAmount(new BigDecimal("5000"))
                .putAttributes("scope", "annual")
                .putAttributes("source", "government_limit")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CONTRIBUTION, RuleSubType.MONETARY, "CONTRIBUTION_LIFETIME_MAX",
            "Total Lifetime Max: EUR 50,000 (across all providers)",
            RuleMeta.builder()
                .maxAmount(new BigDecimal("50000"))
                .putAttributes("scope", "lifetime")
                .putAttributes("cross_provider", "true")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CONTRIBUTION, RuleSubType.MONETARY, "CONTRIBUTION_GOVERNMENT_BONUS",
            "Government Bonus: 4.5% annually on contributions up to EUR 5,000",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0.045"))
                .maxAmount(new BigDecimal("5000"))
                .putAttributes("source", "government")
                .putAttributes("frequency", "annual")
                .putAttributes("calculation", "monthly_paid_quarterly")
                .build()))
                
        // INVESTMENT RULES
        .addRules(ProductRule.of(
            RuleType.INVESTMENT, RuleSubType.OPTIONS, "INVESTMENT_GUARANTEED_PS",
            "Guaranteed PS Option: 1.5% guaranteed + 4.5% government bonus, 0.5% fee",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0.015"))
                .putAttributes("type", "guaranteed")
                .putAttributes("fee", "0.005")
                .putAttributes("government_bonus", "0.045")
                .putAttributes("risk", "none")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.INVESTMENT, RuleSubType.OPTIONS, "INVESTMENT_BALANCED_PS",
            "Balanced PS Fund: 50% bonds, 50% equity + 4.5% government bonus, 1.0% fee",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0.04"))
                .maxAmount(new BigDecimal("0.06"))
                .putAttributes("type", "balanced")
                .putAttributes("asset_mix", "50_bonds_50_equity")
                .putAttributes("fee", "0.01")
                .putAttributes("government_bonus", "0.045")
                .putAttributes("risk", "medium")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.INVESTMENT, RuleSubType.ALLOCATION, "INVESTMENT_ALLOCATION_MIN_PS",
            "Minimum 25% per fund if multiple selected",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0.25"))
                .putAttributes("rule", "minimum_per_fund")
                .putAttributes("applies_when", "multiple_funds")
                .build()))
                
        // FEE RULES
        .addRules(ProductRule.of(
            RuleType.FEE, RuleSubType.ANNUAL, "FEE_ANNUAL_POLICY_PS",
            "Annual Policy Fee: EUR 24 (EUR 2 per month)",
            RuleMeta.builder()
                .minAmount(new BigDecimal("24"))
                .putAttributes("frequency", "annual")
                .putAttributes("monthly_equivalent", "2")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.FEE, RuleSubType.MANAGEMENT, "FEE_MANAGEMENT_PS",
            "Management Fee: 0.5% - 1.2% depending on investment choice",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0.005"))
                .maxAmount(new BigDecimal("0.012"))
                .putAttributes("basis", "investment_dependent")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.FEE, RuleSubType.SETUP, "FEE_CONVERSION_SUBSIDIZED",
            "Conversion Fee: EUR 0 (government subsidized)",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0"))
                .putAttributes("subsidized_by", "government")
                .build()))
                
        // CLAIM RULES
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.WITHDRAWAL, "CLAIM_MINIMUM_HOLDING",
            "Minimum Holding: 10 years from first contribution",
            RuleMeta.builder()
                .putAttributes("minimum_years", "10")
                .putAttributes("type", "holding_period")
                .putAttributes("penalty_if_early", "bonus_forfeiture")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.PENALTY, "CLAIM_EARLY_WITHDRAWAL_BONUS_PENALTY",
            "Early Withdrawal: 20% penalty on government bonus portion",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0.20"))
                .putAttributes("applies_to", "government_bonus_portion")
                .putAttributes("type", "early_withdrawal")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.PAYOUT_OPTIONS, "CLAIM_FULL_WITHDRAWAL",
            "Full Withdrawal: Account value + accumulated government bonus",
            RuleMeta.builder()
                .putAttributes("type", "full_withdrawal")
                .putAttributes("includes", "account_plus_government_bonus")
                .putAttributes("available_at", "maturity")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.PAYOUT_OPTIONS, "CLAIM_PENSION_CONVERSION",
            "Pension Conversion: Convert to life annuity, government bonus remains tax-free",
            RuleMeta.builder()
                .putAttributes("type", "pension_conversion")
                .putAttributes("bonus_treatment", "tax_free")
                .putAttributes("payments_taxed_as", "pension_income")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.TAX_TREATMENT, "CLAIM_TAX_GOVERNMENT_BONUS",
            "Government Bonus: TAX-FREE (4.5% annually)",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0.045"))
                .putAttributes("type", "government_bonus")
                .putAttributes("tax_treatment", "tax_free")
                .putAttributes("scope", "always")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.TAX_TREATMENT, "CLAIM_TAX_INVESTMENT_GROWTH",
            "Investment Growth: Capital gains tax (30%) on growth portion only",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0.30"))
                .putAttributes("type", "capital_gains")
                .putAttributes("scope", "growth_portion_only")
                .build()))
                
        .build();
  }
}