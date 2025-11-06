package io.resys.thena.product.client.api;

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

/**
 * Constants for product rule codes, grouped by category.
 */
public final class ProductRuleCodes {
  
  // ELIGIBILITY RULES
  public static final String CODE_ELIGIBILITY_001_AGE_INCEPTION = "ELIGIBILITY_AGE_INCEPTION";
  public static final String CODE_ELIGIBILITY_002_AGE_MATURITY = "ELIGIBILITY_AGE_MATURITY";
  public static final String CODE_ELIGIBILITY_003_AGE_PAYMENT_START = "ELIGIBILITY_AGE_PAYMENT_START";
  public static final String CODE_ELIGIBILITY_004_AGE_CONVERSION = "ELIGIBILITY_AGE_CONVERSION";
  public static final String CODE_ELIGIBILITY_005_AGE_FLEXIBLE = "ELIGIBILITY_AGE_FLEXIBLE";
  public static final String CODE_ELIGIBILITY_006_RESIDENCY_FINNISH = "ELIGIBILITY_RESIDENCY_FINNISH";
  public static final String CODE_ELIGIBILITY_007_ONE_POLICY = "ELIGIBILITY_ONE_POLICY";
  public static final String CODE_ELIGIBILITY_008_ONE_PS_POLICY = "ELIGIBILITY_ONE_PS_POLICY";
  public static final String CODE_ELIGIBILITY_009_MIN_INVESTMENT = "ELIGIBILITY_MIN_INVESTMENT";
  
  // CONTRIBUTION RULES
  public static final String CODE_CONTRIBUTION_001_INITIAL_RANGE = "CONTRIBUTION_INITIAL_RANGE";
  public static final String CODE_CONTRIBUTION_002_REGULAR_MONTHLY = "CONTRIBUTION_REGULAR_MONTHLY";
  public static final String CODE_CONTRIBUTION_003_TAX_LIMIT = "CONTRIBUTION_TAX_LIMIT";
  public static final String CODE_CONTRIBUTION_004_EMPLOYER_COMBINED = "CONTRIBUTION_EMPLOYER_COMBINED";
  public static final String CODE_CONTRIBUTION_005_MINIMUM_MAINTAIN = "CONTRIBUTION_MINIMUM_MAINTAIN";
  public static final String CODE_CONTRIBUTION_006_ANNUAL_LIMIT = "CONTRIBUTION_ANNUAL_LIMIT";
  public static final String CODE_CONTRIBUTION_007_LIFETIME_MAX = "CONTRIBUTION_LIFETIME_MAX";
  public static final String CODE_CONTRIBUTION_008_GOVERNMENT_BONUS = "CONTRIBUTION_GOVERNMENT_BONUS";
  
  // INVESTMENT RULES
  public static final String CODE_INVESTMENT_001_GUARANTEED = "INVESTMENT_GUARANTEED";
  public static final String CODE_INVESTMENT_002_GUARANTEED_PENSION = "INVESTMENT_GUARANTEED_PENSION";
  public static final String CODE_INVESTMENT_003_GUARANTEED_PS = "INVESTMENT_GUARANTEED_PS";
  public static final String CODE_INVESTMENT_004_BALANCED = "INVESTMENT_BALANCED";
  public static final String CODE_INVESTMENT_005_BALANCED_PS = "INVESTMENT_BALANCED_PS";
  public static final String CODE_INVESTMENT_006_LIFECYCLE = "INVESTMENT_LIFECYCLE";
  public static final String CODE_INVESTMENT_007_BASKETS_GRANITE = "INVESTMENT_BASKETS_GRANITE";
  public static final String CODE_INVESTMENT_008_BASKETS_GLOBE = "INVESTMENT_BASKETS_GLOBE";
  public static final String CODE_INVESTMENT_009_FUNDS_VARIETY = "INVESTMENT_FUNDS_VARIETY";
  public static final String CODE_INVESTMENT_010_ETFS = "INVESTMENT_ETFS";
  public static final String CODE_INVESTMENT_011_INSURANCE_ACCOUNT = "INVESTMENT_INSURANCE_ACCOUNT";
  public static final String CODE_INVESTMENT_012_SWITCHING_TAX_FREE = "INVESTMENT_SWITCHING_TAX_FREE";
  public static final String CODE_INVESTMENT_013_ALLOCATION_MIN = "INVESTMENT_ALLOCATION_MIN";
  public static final String CODE_INVESTMENT_014_ALLOCATION_MIN_PS = "INVESTMENT_ALLOCATION_MIN_PS";
  
  // FEE RULES
  public static final String CODE_FEE_001_ANNUAL_POLICY = "FEE_ANNUAL_POLICY";
  public static final String CODE_FEE_002_ANNUAL_POLICY_PS = "FEE_ANNUAL_POLICY_PS";
  public static final String CODE_FEE_003_MANAGEMENT_BY_FUND = "FEE_MANAGEMENT_BY_FUND";
  public static final String CODE_FEE_004_MANAGEMENT_BY_INVESTMENT = "FEE_MANAGEMENT_BY_INVESTMENT";
  public static final String CODE_FEE_005_MANAGEMENT_PS = "FEE_MANAGEMENT_PS";
  public static final String CODE_FEE_006_MANAGEMENT_STANDARD = "FEE_MANAGEMENT_STANDARD";
  public static final String CODE_FEE_007_MANAGEMENT_UNDER_30 = "FEE_MANAGEMENT_UNDER_30";
  public static final String CODE_FEE_008_MANAGEMENT_MAXIMUM = "FEE_MANAGEMENT_MAXIMUM";
  public static final String CODE_FEE_009_ETF_TRADING = "FEE_ETF_TRADING";
  public static final String CODE_FEE_010_PLEDGING_CONFIRMATION = "FEE_PLEDGING_CONFIRMATION";
  public static final String CODE_FEE_011_CONVERSION_SUBSIDIZED = "FEE_CONVERSION_SUBSIDIZED";
  
  // CLAIM RULES
  public static final String CODE_CLAIM_001_DEATH_BENEFIT = "CLAIM_DEATH_BENEFIT";
  public static final String CODE_CLAIM_002_LIFE_ANNUITY = "CLAIM_LIFE_ANNUITY";
  public static final String CODE_CLAIM_003_TERM_CERTAIN = "CLAIM_TERM_CERTAIN";
  public static final String CODE_CLAIM_004_EARLY_WITHDRAWAL_PENALTY = "CLAIM_EARLY_WITHDRAWAL_PENALTY";
  public static final String CODE_CLAIM_005_EARLY_WITHDRAWAL_BONUS_PENALTY = "CLAIM_EARLY_WITHDRAWAL_BONUS_PENALTY";
  public static final String CODE_CLAIM_006_TAX_INCOME = "CLAIM_TAX_INCOME";
  public static final String CODE_CLAIM_007_TAX_GOVERNMENT_BONUS = "CLAIM_TAX_GOVERNMENT_BONUS";
  public static final String CODE_CLAIM_008_TAX_INVESTMENT_GROWTH = "CLAIM_TAX_INVESTMENT_GROWTH";
  public static final String CODE_CLAIM_009_TAX_PROPORTIONAL = "CLAIM_TAX_PROPORTIONAL";
  public static final String CODE_CLAIM_010_WITHDRAWAL_FLEXIBLE = "CLAIM_WITHDRAWAL_FLEXIBLE";
  public static final String CODE_CLAIM_011_MINIMUM_HOLDING = "CLAIM_MINIMUM_HOLDING";
  public static final String CODE_CLAIM_012_FULL_WITHDRAWAL = "CLAIM_FULL_WITHDRAWAL";
  public static final String CODE_CLAIM_013_PENSION_CONVERSION = "CLAIM_PENSION_CONVERSION";
  public static final String CODE_CLAIM_014_INHERITANCE_NEXT_OF_KIN = "CLAIM_INHERITANCE_NEXT_OF_KIN";
  public static final String CODE_CLAIM_015_INHERITANCE_OTHERS = "CLAIM_INHERITANCE_OTHERS";
  public static final String CODE_CLAIM_016_INHERITANCE_COVER = "CLAIM_INHERITANCE_COVER";
  public static final String CODE_CLAIM_017_BENEFICIARY_CLAUSE = "CLAIM_BENEFICIARY_CLAUSE";
  
  private ProductRuleCodes() {
    // Utility class - no instantiation
  }
}