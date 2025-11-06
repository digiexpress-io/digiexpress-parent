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

public class Product_Nova_Virtus {
  
  public static Product create() {
    return Product.builder()
        .productCode("NOVA_VIR_001")
        .productName("Nova Virtus Säästöhenkivakuutus")
        .category("Unit-linked Endowment Insurance")
        .target("Long-term wealth accumulation with inheritance planning")
        .description(ProductSamples.loadResourceText("nova-virtus-endowment.txt"))
        
        // ELIGIBILITY RULES
        .addRules(ProductRule.of(
            RuleType.ELIGIBILITY, RuleSubType.AGE, "ELIGIBILITY_AGE_FLEXIBLE",
            "Age Range: No specific limits mentioned",
            RuleMeta.builder()
                .putAttributes("flexibility", "no_limits_specified")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.ELIGIBILITY, RuleSubType.RESIDENCY, "ELIGIBILITY_RESIDENCY_FINNISH",
            "Available to Finnish residents",
            RuleMeta.builder()
                .putAttributes("required", "finnish_resident")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.ELIGIBILITY, RuleSubType.MONETARY, "ELIGIBILITY_MIN_INVESTMENT",
            "Min Investment: EUR 10 (minimum amount for investment)",
            RuleMeta.builder()
                .minAmount(new BigDecimal("10"))
                .putAttributes("type", "minimum_per_investment")
                .build()))
                
        // FEE RULES (Nova has complex age-tiered structure)
        .addRules(ProductRule.of(
            RuleType.FEE, RuleSubType.MANAGEMENT, "FEE_MANAGEMENT_STANDARD",
            "Standard customers: 0.40% up to EUR 100,000, 0.10% above",
            RuleMeta.builder()
                .putAttributes("customer_type", "standard")
                .putAttributes("rate_low", "0.004")
                .putAttributes("rate_high", "0.001")
                .putAttributes("threshold", "100000")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.FEE, RuleSubType.MANAGEMENT, "FEE_MANAGEMENT_UNDER_30",
            "Under 30 years: 0.20% up to EUR 100,000, 0.10% above",
            RuleMeta.builder()
                .endAge(30)
                .putAttributes("customer_type", "under_30")
                .putAttributes("rate_low", "0.002")
                .putAttributes("rate_high", "0.001")
                .putAttributes("threshold", "100000")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.FEE, RuleSubType.MANAGEMENT, "FEE_MANAGEMENT_MAXIMUM",
            "Maximum management fee: 1% of insurance savings",
            RuleMeta.builder()
                .maxAmount(new BigDecimal("0.01"))
                .putAttributes("type", "maximum_allowed")
                .putAttributes("applies_to", "insurance_savings")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.FEE, RuleSubType.TRANSACTION, "FEE_ETF_TRADING",
            "ETF trading fee: 0.15% of transaction amount (one-off)",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0.0015"))
                .putAttributes("type", "etf_trading")
                .putAttributes("frequency", "one_off")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.FEE, RuleSubType.TRANSACTION, "FEE_PLEDGING_CONFIRMATION",
            "Pledging confirmation: EUR 50",
            RuleMeta.builder()
                .minAmount(new BigDecimal("50"))
                .putAttributes("type", "pledging_confirmation")
                .build()))
                
        // INVESTMENT RULES (Nova has extensive options)
        .addRules(ProductRule.of(
            RuleType.INVESTMENT, RuleSubType.OPTIONS, "INVESTMENT_BASKETS_GRANITE",
            "Granite Portfolios: Broadly diversified, enhanced through real estate",
            RuleMeta.builder()
                .putAttributes("type", "granite_portfolios")
                .putAttributes("characteristics", "broadly_diversified")
                .putAttributes("enhancement", "real_estate")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.INVESTMENT, RuleSubType.OPTIONS, "INVESTMENT_BASKETS_GLOBE",
            "Globe Baskets: ESG characteristics, excellent diversification",
            RuleMeta.builder()
                .putAttributes("type", "globe_baskets")
                .putAttributes("characteristics", "esg_environmental_social")
                .putAttributes("diversification", "excellent")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.INVESTMENT, RuleSubType.OPTIONS, "INVESTMENT_FUNDS_VARIETY",
            "Investment Funds: Nearly 400 funds available",
            RuleMeta.builder()
                .putAttributes("type", "investment_funds")
                .putAttributes("count", "400")
                .putAttributes("variety", "risk_tolerance_goal_matching")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.INVESTMENT, RuleSubType.OPTIONS, "INVESTMENT_ETFS",
            "ETFs: 35 exchange-traded funds, monthly saving possible",
            RuleMeta.builder()
                .putAttributes("type", "etf")
                .putAttributes("count", "35")
                .putAttributes("monthly_saving", "true")
                .putAttributes("minimum", "none")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.INVESTMENT, RuleSubType.OPTIONS, "INVESTMENT_INSURANCE_ACCOUNT",
            "Insurance Account: Low-risk, 1-week Euribor minus 0.4%",
            RuleMeta.builder()
                .putAttributes("type", "insurance_account")
                .putAttributes("risk", "low")
                .putAttributes("yield", "1w_euribor_minus_0_4")
                .putAttributes("purpose", "temporary_parking")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.INVESTMENT, RuleSubType.SWITCHING, "INVESTMENT_SWITCHING_TAX_FREE",
            "Investment switching: Tax-free switching between options",
            RuleMeta.builder()
                .putAttributes("tax_treatment", "tax_free")
                .putAttributes("restriction", "none")
                .build()))
                
        // CLAIM RULES (Nova has inheritance focus)
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.WITHDRAWAL, "CLAIM_WITHDRAWAL_FLEXIBLE",
            "Flexible withdrawal: Available anytime",
            RuleMeta.builder()
                .putAttributes("flexibility", "anytime")
                .putAttributes("restrictions", "none_mentioned")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.TAX_TREATMENT, "CLAIM_TAX_PROPORTIONAL",
            "Proportional calculation between capital and taxable return",
            RuleMeta.builder()
                .putAttributes("type", "proportional_calculation")
                .putAttributes("example", "10k_invested_12k_value_1200_withdrawal_1000_capital_200_taxable")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.DEATH_BENEFIT, "CLAIM_INHERITANCE_NEXT_OF_KIN",
            "Death benefit to next-of-kin: Subject to inheritance tax",
            RuleMeta.builder()
                .putAttributes("beneficiary_type", "next_of_kin")
                .putAttributes("tax_treatment", "inheritance_tax")
                .putAttributes("definition", "spouse_direct_heirs_adopted_foster")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.DEATH_BENEFIT, "CLAIM_INHERITANCE_OTHERS",
            "Non-next-of-kin beneficiaries: Capital income tax rate applied",
            RuleMeta.builder()
                .putAttributes("beneficiary_type", "non_next_of_kin")
                .putAttributes("tax_treatment", "capital_income_tax")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.DEATH_BENEFIT, "CLAIM_INHERITANCE_COVER",
            "Inheritance Cover: Protects invested capital, only charged when needed",
            RuleMeta.builder()
                .putAttributes("type", "capital_protection")
                .putAttributes("charging", "conditional_when_savings_less_than_capital")
                .putAttributes("cost", "free_if_growth")
                .build()))
                
        // INHERITANCE PLANNING FEATURES
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.DEATH_BENEFIT, "CLAIM_BENEFICIARY_CLAUSE",
            "Beneficiary clause: Choose distribution without separate will",
            RuleMeta.builder()
                .putAttributes("type", "beneficiary_clause")
                .putAttributes("flexibility", "distribute_to_grandchildren")
                .putAttributes("amendable", "according_to_life_situation")
                .putAttributes("marital_rights", "can_exclude_spouse_rights")
                .build()))
                
        .build();
  }
}