package io.resys.thena.contract.samples;

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

import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.product.client.api.Product.AgeRange;
import io.resys.thena.product.client.api.Product.IncomeRange;

@Value.Immutable
@JsonSerialize(as = ImmutableGenerationOptions.class)
@JsonDeserialize(as = ImmutableGenerationOptions.class)
public interface GenerationOptions {
  
  Optional<AgeRange> getAgeRange();
  Optional<IncomeRange> getIncomeRange();
  
  @Value.Default
  default boolean isIncludeBeneficiaries() { return true; }
  
  @Value.Default
  default String getRiskProfile() { return "MODERATE"; }
  
  
  static ImmutableGenerationOptions.Builder builder() {
    return ImmutableGenerationOptions.builder();
  }
  
  static GenerationOptions defaults() {
    return builder().build();
  }
  

}