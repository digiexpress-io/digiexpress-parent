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

public enum RuleType {
  
  ELIGIBILITY("Eligibility requirements and restrictions"),
  CONTRIBUTION("Contribution amounts, limits and payment rules"),
  FEE("Fee structures and cost calculations"),
  INVESTMENT("Investment options and allocation rules"),
  CLAIM("Claim processing, withdrawals and payouts");
  
  private final String description;
  
  RuleType(String description) {
    this.description = description;
  }
  
  public String getDescription() {
    return description;
  }
}