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
  
  static ImmutableProduct.Builder builder() {
    return ImmutableProduct.builder();
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
}