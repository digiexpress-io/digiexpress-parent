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

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableRuleMeta.class)
@JsonDeserialize(as = ImmutableRuleMeta.class)
public interface RuleMeta {
  
  Optional<Integer> getStartAge();
  Optional<Integer> getEndAge();
  Optional<BigDecimal> getMinAmount();
  Optional<BigDecimal> getMaxAmount();
  Map<String, String> getAttributes();
  
  static ImmutableRuleMeta.Builder builder() {
    return ImmutableRuleMeta.builder();
  }
  
  static RuleMeta empty() {
    return builder().build();
  }
  
  static RuleMeta ofAge(Integer startAge, Integer endAge) {
    return builder()
        .startAge(startAge)
        .endAge(endAge)
        .build();
  }
  
  static RuleMeta ofAmount(BigDecimal minAmount, BigDecimal maxAmount) {
    return builder()
        .minAmount(minAmount)
        .maxAmount(maxAmount)
        .build();
  }
  
  static RuleMeta ofAttributes(Map<String, String> attributes) {
    return builder()
        .putAllAttributes(attributes)
        .build();
  }
}