package io.resys.lp.client.spi;

/*-
 * #%L
 * lp-client
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.resys.lp.client.api.LpClient.FundQuery;
import io.resys.lp.client.api.entities.Fund;
import io.resys.lp.client.api.entities.Fund.FundValueType;
import io.resys.lp.client.api.entities.ImmutableFund;
import io.resys.lp.client.api.entities.ImmutableFundValue;
import io.resys.lp.product.spi.providers.Fund_Provider;

public class FundQueryImpl implements FundQuery {
  private final LocalDate NOW = LocalDate.now();
  
  // Fund cache: key = fundId, value = cached fund with historical values
  private static final Map<String, Fund> FUND_CACHE = new ConcurrentHashMap<>();

  @Override
  public Fund getOne(String fundIdNameOrCode, LocalDate targetDate) {
    
    /**
     * ctx.getLedger().getUnitPrices().stream()
        .filter(up -> up.getExternalId().equals(allocation.getInvPlanAllocCode()))
        .findFirst()
        .map(up -> up.getUnitValue())
        .orElse(BigDecimal.ONE)
     */
    
    
    if (fundIdNameOrCode == null) {
      throw new IllegalArgumentException("Fund ID cannot be null");
    }
    if (targetDate == null) {
      targetDate = LocalDate.now();
    }
    
    // 1. Find fund info from Fund_Provider
    final var fundInfo = findFundByNameOrCode(fundIdNameOrCode);
    final var fundId = fundInfo.getCode();
    
    // 2. Check if fund is cached
    var cachedFund = FUND_CACHE.get(fundId);
    if (cachedFund == null) {
      // Create new fund and cache it
      cachedFund = createBaseFund(fundInfo);
      FUND_CACHE.put(fundId, cachedFund);
    }
    
    // 3. Check if target date value exists in cache
    final var targetValue = findValueForDate(cachedFund, targetDate);
    if (targetValue != null) {
      // 4. Make a copy with correct calculation value
      return ImmutableFund.copyOf(cachedFund)
          .withCalculationValue(targetValue);
    }
    
    // Generate new value for target date and update cache
    final var newValue = generateValueForDate(fundInfo, targetDate);
    final var updatedValues = new ArrayList<>(cachedFund.getRelevantValues());
    updatedValues.add(newValue);
    
    final var updatedFund = ImmutableFund.copyOf(cachedFund)
        .withRelevantValues(updatedValues);
    
    FUND_CACHE.put(fundId, updatedFund);
    
    return ImmutableFund.copyOf(updatedFund)
        .withCalculationValue(newValue);
  }
  
  private Fund_Provider.FundInfo findFundByNameOrCode(String nameOrCode) {
    if (nameOrCode == null) {
      return Fund_Provider.FUND_INFO[1]; // Default to TASAPAINOINEN_RAHASTO
    }
    
    // Direct match first (code or name - case insensitive)
    for (Fund_Provider.FundInfo fundInfo : Fund_Provider.FUND_INFO) {
      if (fundInfo.getCode().equalsIgnoreCase(nameOrCode) || 
          fundInfo.getName().equalsIgnoreCase(nameOrCode)) {
        return fundInfo;
      }
    }
    
    return Fund_Provider.FUND_INFO[1]; // Default to balanced fund
  }
  
  private Fund createBaseFund(Fund_Provider.FundInfo fundInfo) {
    return ImmutableFund.builder()
        .id(fundInfo.getCode())
        .calculationValue(ImmutableFundValue.builder()
            .priceDate(NOW)
            .priceType(FundValueType.ESTIMATE)
            .priceValue(BigDecimal.ZERO)
            .build()) // Will be set when returning
        .build();
  }
  
  private ImmutableFundValue findValueForDate(Fund fund, LocalDate date) {
    return (ImmutableFundValue) fund.getRelevantValues().stream()
        .filter(value -> value.getPriceDate().equals(date))
        .findFirst()
        .orElse(null);
  }
  
  private ImmutableFundValue generateValueForDate(Fund_Provider.FundInfo fundInfo, LocalDate date) {
    final var baseValue = fundInfo.getBaseValue();
    final var volatility = fundInfo.getVolatility();
    final var calculatedValue = calculateFundValue(baseValue, volatility, date, fundInfo.getCode());
    
    return ImmutableFundValue.builder()
        .priceDate(date)
        .priceValue(calculatedValue)
        .priceType(FundValueType.ESTIMATE)
        .build();
  }
  
  
  private BigDecimal calculateFundValue(BigDecimal baseValue, BigDecimal volatility, LocalDate date, String fundCode) {
    // Use date as seed for consistent but "random" values
    final var seed = date.toEpochDay() + fundCode.hashCode();
    
    // Pseudo-random movement based on date seed (deterministic but varied)
    // Using multiple sine waves for more realistic movement patterns
    final var random = (Math.sin(seed * 0.1) + Math.sin(seed * 0.023) + Math.sin(seed * 0.007)) / 3.0;
    
    // Apply volatility (scaled by random movement)
    final var dailyChange = volatility.multiply(BigDecimal.valueOf(random));
    
    // Calculate final value with floor protection (never below 20% of base value)
    final var newValue = baseValue.add(baseValue.multiply(dailyChange));
    final var minValue = baseValue.multiply(new BigDecimal("0.2"));
    
    return newValue.max(minValue).setScale(2, RoundingMode.HALF_UP);
  }
}
