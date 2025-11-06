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

import io.resys.thena.product.client.api.ImmutableProduct;
import io.resys.thena.product.client.api.Product;
import io.resys.thena.product.client.api.ProductRule;
import io.resys.thena.product.client.api.RuleMeta;
import io.resys.thena.product.client.api.RuleSubType;
import io.resys.thena.product.client.api.RuleType;

public class Product_Feemi_Pension {
  
  public static Product create() {
    return ImmutableProduct.builder()
        .productCode("FEEMI_PEN_001")
        .productName("Vapaaehtoinen eläkevakuutus")
        .category("Individual Pension Insurance")
        .target("Retirement savings with tax benefits")
        .description(ProductSamples.loadResourceText("feemi-pension-insurance.txt"))
        
        // ELIGIBILITY RULES
        .addRules(ProductRule.of(
            RuleType.ELIGIBILITY, RuleSubType.AGE, "ELIGIBILITY_AGE_INCEPTION",
            "Age Range: 18 - 62 years at inception",
            RuleMeta.builder().startAge(18).endAge(62).build()))
            
        .addRules(ProductRule.of(
            RuleType.ELIGIBILITY, RuleSubType.AGE, "ELIGIBILITY_AGE_PAYMENT_START",
            "Must start payments before age 68",
            RuleMeta.builder()
                .endAge(68)
                .putAttributes("type", "payment_deadline")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.ELIGIBILITY, RuleSubType.RESTRICTIONS, "ELIGIBILITY_ONE_POLICY",
            "One active pension insurance per person",
            RuleMeta.builder()
                .putAttributes("limit", "one_per_person")
                .build()))
                
        // CONTRIBUTION RULES
        .addRules(ProductRule.of(
            RuleType.CONTRIBUTION, RuleSubType.AMOUNT, "CONTRIBUTION_INITIAL_RANGE",
            "Initial Payment: EUR 500 - EUR 8,500 (annual tax limit)",
            RuleMeta.builder()
                .minAmount(new BigDecimal("500"))
                .maxAmount(new BigDecimal("8500"))
                .putAttributes("type", "initial")
                .putAttributes("tax_limit", "annual")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CONTRIBUTION, RuleSubType.MONETARY, "CONTRIBUTION_TAX_LIMIT",
            "Annual Tax Limit: EUR 8,500 per year (Finnish tax code)",
            RuleMeta.builder()
                .maxAmount(new BigDecimal("8500"))
                .putAttributes("scope", "annual")
                .putAttributes("tax_deductible", "true")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CONTRIBUTION, RuleSubType.MONETARY, "CONTRIBUTION_EMPLOYER_COMBINED",
            "Combined Max: EUR 17,000 per year (employee + employer)",
            RuleMeta.builder()
                .maxAmount(new BigDecimal("17000"))
                .putAttributes("type", "combined_employee_employer")
                .putAttributes("scope", "annual")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CONTRIBUTION, RuleSubType.MONETARY, "CONTRIBUTION_MINIMUM_MAINTAIN",
            "Minimum to Maintain: EUR 300 per year (or contract lapses)",
            RuleMeta.builder()
                .minAmount(new BigDecimal("300"))
                .putAttributes("scope", "annual")
                .putAttributes("consequence", "contract_lapses")
                .build()))
                
        // INVESTMENT RULES
        .addRules(ProductRule.of(
            RuleType.INVESTMENT, RuleSubType.OPTIONS, "INVESTMENT_GUARANTEED_PENSION",
            "Guaranteed Pension: 1.0% guaranteed annually, 0.9% fee",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0.01"))
                .putAttributes("type", "guaranteed")
                .putAttributes("fee", "0.009")
                .putAttributes("risk", "none")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.INVESTMENT, RuleSubType.OPTIONS, "INVESTMENT_LIFECYCLE",
            "Lifecycle Option: Automatic rebalancing based on age, 1.2% fee",
            RuleMeta.builder()
                .putAttributes("type", "lifecycle")
                .putAttributes("fee", "0.012")
                .putAttributes("rebalancing", "automatic_age_based")
                .build()))
                
        // FEE RULES
        .addRules(ProductRule.of(
            RuleType.FEE, RuleSubType.ANNUAL, "FEE_ANNUAL_POLICY",
            "Annual Policy Fee: EUR 48 (EUR 4 per month)",
            RuleMeta.builder()
                .minAmount(new BigDecimal("48"))
                .putAttributes("frequency", "annual")
                .putAttributes("monthly_equivalent", "4")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.FEE, RuleSubType.MANAGEMENT, "FEE_MANAGEMENT_BY_INVESTMENT",
            "Management Fee: 0.9% - 1.4% depending on investment choice",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0.009"))
                .maxAmount(new BigDecimal("0.014"))
                .putAttributes("basis", "investment_dependent")
                .build()))
                
        // CLAIM RULES
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.PAYOUT_OPTIONS, "CLAIM_LIFE_ANNUITY",
            "Life Annuity: Monthly payments for life with 10-year guarantee",
            RuleMeta.builder()
                .putAttributes("type", "life_annuity")
                .putAttributes("guarantee", "10_years")
                .putAttributes("frequency", "monthly")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.PAYOUT_OPTIONS, "CLAIM_TERM_CERTAIN",
            "Term Certain Annuity: Fixed period 5, 10, 15, or 20 years",
            RuleMeta.builder()
                .putAttributes("type", "term_certain")
                .putAttributes("periods", "5,10,15,20")
                .putAttributes("higher_payments", "true")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.PENALTY, "CLAIM_EARLY_WITHDRAWAL_PENALTY",
            "Before age 62: 20% penalty + income tax",
            RuleMeta.builder()
                .minAmount(new BigDecimal("0.20"))
                .endAge(62)
                .putAttributes("type", "early_withdrawal")
                .putAttributes("additional_tax", "income_tax")
                .build()))
                
        .addRules(ProductRule.of(
            RuleType.CLAIM, RuleSubType.TAX_TREATMENT, "CLAIM_TAX_INCOME",
            "Retirement Payments: Taxed as INCOME (marginal tax rate)",
            RuleMeta.builder()
                .putAttributes("type", "income_tax")
                .putAttributes("rate", "marginal")
                .putAttributes("scope", "retirement_payments")
                .build()))
                
        .build();
  }
}