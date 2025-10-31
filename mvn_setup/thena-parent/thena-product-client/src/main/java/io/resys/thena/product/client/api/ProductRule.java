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

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableProductRule.class)
@JsonDeserialize(as = ImmutableProductRule.class)
public interface ProductRule {
  
  RuleType getType();
  RuleSubType getSubType();
  String getRuleCode();
  String getText();
  RuleMeta getMeta();
  
  static ImmutableProductRule.Builder builder() {
    return ImmutableProductRule.builder();
  }
  
  static ProductRule of(RuleType type, RuleSubType subType, String ruleCode, String text) {
    return builder()
        .type(type)
        .subType(subType)
        .ruleCode(ruleCode)
        .text(text)
        .meta(RuleMeta.empty())
        .build();
  }
  
  static ProductRule of(RuleType type, RuleSubType subType, String ruleCode, String text, RuleMeta meta) {
    return builder()
        .type(type)
        .subType(subType)
        .ruleCode(ruleCode)
        .text(text)
        .meta(meta)
        .build();
  }
  
  static ProductRule withAge(RuleType type, RuleSubType subType, String ruleCode, String text, 
                             Integer startAge, Integer endAge) {
    return of(type, subType, ruleCode, text, RuleMeta.ofAge(startAge, endAge));
  }
  
  static ProductRule withAmount(RuleType type, RuleSubType subType, String ruleCode, String text, 
                                BigDecimal minAmount, BigDecimal maxAmount) {
    return of(type, subType, ruleCode, text, RuleMeta.ofAmount(minAmount, maxAmount));
  }
  
  static ProductRule withAttributes(RuleType type, RuleSubType subType, String ruleCode, String text, 
                                    Map<String, String> attributes) {
    return of(type, subType, ruleCode, text, RuleMeta.ofAttributes(attributes));
  }
}