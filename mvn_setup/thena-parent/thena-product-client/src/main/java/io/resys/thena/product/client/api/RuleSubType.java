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

public enum RuleSubType {
  
  // Eligibility subtypes
  AGE("Age-related eligibility requirements"),
  RESIDENCY("Residency and tax status requirements"),
  HEALTH("Health and medical underwriting requirements"),
  EMPLOYMENT("Employment status requirements"),
  RESTRICTIONS("Policy ownership and quantity restrictions"),
  
  // Contribution subtypes  
  AMOUNT("Contribution amount ranges and limits"),
  FREQUENCY("Payment frequency and timing rules"),
  MONETARY("Monetary limits and caps"),
  METHODS("Allowed payment methods"),
  
  // Fee subtypes
  SETUP("Setup and initialization fees"),
  ANNUAL("Annual and recurring fees"),
  MANAGEMENT("Management and administration fees"),
  TRANSACTION("Transaction-based fees"),
  PENALTY("Penalty and early withdrawal fees"),
  
  // Investment subtypes
  OPTIONS("Available investment options and funds"),
  ALLOCATION("Investment allocation rules and requirements"),
  SWITCHING("Investment switching and rebalancing rules"),
  
  // Claim subtypes
  WITHDRAWAL("Withdrawal rules and procedures"),
  DEATH_BENEFIT("Death benefit and inheritance rules"),
  TAX_TREATMENT("Tax treatment and calculation rules"),
  PAYOUT_OPTIONS("Payout methods and annuity options");
  
  private final String description;
  
  RuleSubType(String description) {
    this.description = description;
  }
  
  public String getDescription() {
    return description;
  }
}