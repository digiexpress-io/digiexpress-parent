package io.resys.lp.product.spi.providers;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;

import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public class Fund_Provider {
  
  private static final Random RANDOM = new Random();
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final Faker FAKER = new Faker(Locale.of("fi", "FI"));
  
  @lombok.Value
  public static class FundInfo {
    String code;
    String name;
    String riskLevel;
    BigDecimal baseValue;
    BigDecimal volatility;
  }
  
  public static final FundInfo[] FUND_INFO = {
    new FundInfo("TAATTU_TUOTTO", "Taattu tuotto", "NONE", new BigDecimal("100.50"), new BigDecimal("0.005")),
    new FundInfo("TASAPAINOINEN_RAHASTO", "Tasapainoinen rahasto", "MEDIUM", new BigDecimal("125.75"), new BigDecimal("0.015")),
    new FundInfo("OSAKERAHASTO", "Osakerahasto", "HIGH", new BigDecimal("180.25"), new BigDecimal("0.025")),
    new FundInfo("INDEKSIRAHASTO", "Indeksirahasto", "HIGH", new BigDecimal("165.80"), new BigDecimal("0.020")),
    new FundInfo("EUROOPPA_RAHASTO", "Eurooppa-rahasto", "MEDIUM", new BigDecimal("140.60"), new BigDecimal("0.022")),
    new FundInfo("KEHITTYVAT_MARKKINAT", "Kehittyvät markkinat", "VERY_HIGH", new BigDecimal("195.30"), new BigDecimal("0.040")),
    new FundInfo("OBLIGAATIORAHASTO", "Obligaatiorahasto", "LOW", new BigDecimal("105.20"), new BigDecimal("0.008")),
    new FundInfo("KIINTEISTO_RAHASTO", "Kiinteistörahasto", "HIGH", new BigDecimal("155.90"), new BigDecimal("0.018")),
    new FundInfo("TEKNOLOGIA_RAHASTO", "Teknologia-rahasto", "VERY_HIGH", new BigDecimal("220.45"), new BigDecimal("0.045")),
    new FundInfo("TERVEYDENHUOLTO_RAHASTO", "Terveydenhuolto-rahasto", "HIGH", new BigDecimal("175.80"), new BigDecimal("0.028")),
    new FundInfo("VASTUULLINEN_SIJOITTAMINEN", "Vastuullinen sijoittaminen", "MEDIUM", new BigDecimal("160.30"), new BigDecimal("0.020"))
  };
  

  @Data
  @Builder
  @Jacksonized
  public static class Fund {
    private String fundCode;
    private String fundName;
    private String currency;
    private String riskLevel;
    private BigDecimal managementFee;
    private BigDecimal performanceFee;
    private BigDecimal minInvestment;
    private BigDecimal currentValue;
    private String description;
    private String category;
    private String manager;
    private LocalDate inceptionDate;
    
    public JsonObject toJson() {
      try {
        String json = MAPPER.writeValueAsString(this);
        return new JsonObject(json);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Failed to convert Fund to JSON", e);
      }
    }
    
    public Map<String, Object> toMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("fundCode", fundCode);
      map.put("fundName", fundName);
      map.put("currency", currency);
      map.put("riskLevel", riskLevel);
      map.put("managementFee", managementFee);
      map.put("performanceFee", performanceFee);
      map.put("minInvestment", minInvestment);
      map.put("currentValue", currentValue);
      map.put("description", description);
      map.put("category", category);
      map.put("manager", manager);
      map.put("inceptionDate", inceptionDate.toString());
      return map;
    }
  }
  
  @Data
  @Builder
  @Jacksonized
  public static class InvestmentAllocation {
    private String fundCode;
    private String fundName;
    private BigDecimal percentage;
    private BigDecimal amount;
    private String riskLevel;
    private LocalDate allocationDate;
    
    public JsonObject toJson() {
      try {
        String json = MAPPER.writeValueAsString(this);
        return new JsonObject(json);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Failed to convert InvestmentAllocation to JSON", e);
      }
    }
    
    public Map<String, Object> toMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("fundCode", fundCode);
      map.put("fundName", fundName);
      map.put("percentage", percentage);
      map.put("amount", amount);
      map.put("riskLevel", riskLevel);
      map.put("allocationDate", allocationDate.toString());
      return map;
    }
  }
  
  @Data
  @Builder
  @Jacksonized
  public static class PaymentPlan {
    private BigDecimal monthlyAmount;
    private BigDecimal annualAmount;
    private String frequency;
    private LocalDate startDate;
    private LocalDate nextPaymentDate;
    private String paymentMethod;
    private String bankAccount;
    private boolean active;
    
    public JsonObject toJson() {
      try {
        String json = MAPPER.writeValueAsString(this);
        return new JsonObject(json);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Failed to convert PaymentPlan to JSON", e);
      }
    }
    
    public Map<String, Object> toMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("monthlyAmount", monthlyAmount);
      map.put("annualAmount", annualAmount);
      map.put("frequency", frequency);
      map.put("startDate", startDate.toString());
      map.put("nextPaymentDate", nextPaymentDate.toString());
      map.put("paymentMethod", paymentMethod);
      map.put("bankAccount", bankAccount);
      map.put("active", active);
      return map;
    }
  }
  
  @Data
  @Builder
  @Jacksonized
  public static class Coverage {
    private String coverageType;
    private String coverageCode;
    private BigDecimal sumInsured;
    private BigDecimal premium;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private boolean active;
    
    public JsonObject toJson() {
      try {
        String json = MAPPER.writeValueAsString(this);
        return new JsonObject(json);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Failed to convert Coverage to JSON", e);
      }
    }
    
    public Map<String, Object> toMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("coverageType", coverageType);
      map.put("coverageCode", coverageCode);
      map.put("sumInsured", sumInsured);
      map.put("premium", premium);
      map.put("effectiveDate", effectiveDate.toString());
      map.put("expiryDate", expiryDate.toString());
      map.put("active", active);
      return map;
    }
  }
  
  public static Fund generateFund(String riskProfile) {
    int fundIndex = RANDOM.nextInt(FUND_INFO.length);
    String fundCode = FUND_INFO[fundIndex].getCode();
    String fundName = FUND_INFO[fundIndex].getName();
    
    String riskLevel = determineRiskLevel(fundCode, riskProfile);
    BigDecimal managementFee = generateManagementFee(riskLevel);
    BigDecimal performanceFee = generatePerformanceFee(riskLevel);
    
    return Fund.builder()
        .fundCode(fundCode)
        .fundName(fundName)
        .currency("EUR")
        .riskLevel(riskLevel)
        .managementFee(managementFee)
        .performanceFee(performanceFee)
        .minInvestment(new BigDecimal("100"))
        .currentValue(generateCurrentValue())
        .description(generateDescription(fundName, riskLevel))
        .category(generateCategory(fundCode))
        .manager(FAKER.company().name() + " Asset Management")
        .inceptionDate(generateInceptionDate())
        .build();
  }
  
  public static List<InvestmentAllocation> generateAllocations(String riskProfile, BigDecimal totalAmount) {
    List<InvestmentAllocation> allocations = new ArrayList<>();
    
    switch (riskProfile) {
      case "CONSERVATIVE" -> {
        allocations.add(createAllocation("TAATTU_TUOTTO", "Taattu tuotto", new BigDecimal("0.70"), totalAmount, "NONE"));
        allocations.add(createAllocation("OBLIGAATIORAHASTO", "Obligaatiorahasto", new BigDecimal("0.30"), totalAmount, "LOW"));
      }
      case "AGGRESSIVE" -> {
        allocations.add(createAllocation("OSAKERAHASTO", "Osakerahasto", new BigDecimal("0.60"), totalAmount, "HIGH"));
        allocations.add(createAllocation("KEHITTYVAT_MARKKINAT", "Kehittyvät markkinat", new BigDecimal("0.40"), totalAmount, "VERY_HIGH"));
      }
      default -> { // MODERATE
        allocations.add(createAllocation("TASAPAINOINEN_RAHASTO", "Tasapainoinen rahasto", new BigDecimal("0.50"), totalAmount, "MEDIUM"));
        allocations.add(createAllocation("OSAKERAHASTO", "Osakerahasto", new BigDecimal("0.30"), totalAmount, "HIGH"));
        allocations.add(createAllocation("TAATTU_TUOTTO", "Taattu tuotto", new BigDecimal("0.20"), totalAmount, "NONE"));
      }
    }
    
    return allocations;
  }
  
  public static PaymentPlan generatePaymentPlan(int annualIncome) {
    double contributionRate = 0.03 + (RANDOM.nextDouble() * 0.05);
    int monthlyAmount = (int) (annualIncome * contributionRate / 12);
    monthlyAmount = Math.round(monthlyAmount / 25.0f) * 25;
    monthlyAmount = Math.max(50, Math.min(5000, monthlyAmount));
    
    LocalDate startDate = LocalDate.now().minusDays(RANDOM.nextInt(365));
    
    return PaymentPlan.builder()
        .monthlyAmount(new BigDecimal(monthlyAmount))
        .annualAmount(new BigDecimal(monthlyAmount * 12))
        .frequency("MONTHLY")
        .startDate(startDate)
        .nextPaymentDate(calculateNextPaymentDate(startDate))
        .paymentMethod("DIRECT_DEBIT")
        .bankAccount(generateBankAccount())
        .active(true)
        .build();
  }
  
  public static Coverage generateCoverage(String productCode) {
    String coverageType = determineCoverageType(productCode);
    BigDecimal sumInsured = generateSumInsured(coverageType);
    LocalDate effectiveDate = LocalDate.now().minusDays(RANDOM.nextInt(730));
    
    return Coverage.builder()
        .coverageType(coverageType)
        .coverageCode(generateCoverageCode(coverageType))
        .sumInsured(sumInsured)
        .premium(calculatePremium(sumInsured, coverageType))
        .effectiveDate(effectiveDate)
        .expiryDate(effectiveDate.plusYears(1))
        .active(true)
        .build();
  }
  
  private static InvestmentAllocation createAllocation(String code, String name, BigDecimal percentage, BigDecimal totalAmount, String riskLevel) {
    BigDecimal amount = totalAmount.multiply(percentage);
    
    return InvestmentAllocation.builder()
        .fundCode(code)
        .fundName(name)
        .percentage(percentage)
        .amount(amount)
        .riskLevel(riskLevel)
        .allocationDate(LocalDate.now().minusDays(RANDOM.nextInt(365)))
        .build();
  }
  
  private static String determineRiskLevel(String fundCode, String riskProfile) {
    return switch (fundCode) {
      case "TAATTU_TUOTTO" -> "NONE";
      case "OBLIGAATIORAHASTO" -> "LOW";
      case "TASAPAINOINEN_RAHASTO", "EUROOPPA_RAHASTO" -> "MEDIUM";
      case "OSAKERAHASTO", "INDEKSIRAHASTO", "KIINTEISTO_RAHASTO" -> "HIGH";
      case "KEHITTYVAT_MARKKINAT", "TEKNOLOGIA_RAHASTO", "TERVEYDENHUOLTO_RAHASTO" -> "VERY_HIGH";
      default -> "MEDIUM";
    };
  }
  
  private static BigDecimal generateManagementFee(String riskLevel) {
    return switch (riskLevel) {
      case "NONE" -> new BigDecimal("0.005");
      case "LOW" -> new BigDecimal("0.008");
      case "MEDIUM" -> new BigDecimal("0.012");
      case "HIGH" -> new BigDecimal("0.015");
      case "VERY_HIGH" -> new BigDecimal("0.020");
      default -> new BigDecimal("0.012");
    };
  }
  
  private static BigDecimal generatePerformanceFee(String riskLevel) {
    return switch (riskLevel) {
      case "NONE", "LOW" -> BigDecimal.ZERO;
      case "MEDIUM" -> new BigDecimal("0.05");
      case "HIGH" -> new BigDecimal("0.10");
      case "VERY_HIGH" -> new BigDecimal("0.15");
      default -> BigDecimal.ZERO;
    };
  }
  
  private static BigDecimal generateCurrentValue() {
    double baseValue = 100.0;
    double variance = (RANDOM.nextDouble() - 0.5) * 40.0;
    return new BigDecimal(String.format("%.2f", baseValue + variance));
  }
  
  private static String generateDescription(String fundName, String riskLevel) {
    String riskDesc = switch (riskLevel) {
      case "NONE" -> "capital protected";
      case "LOW" -> "low risk";
      case "MEDIUM" -> "moderate risk";
      case "HIGH" -> "high risk";
      case "VERY_HIGH" -> "very high risk";
      default -> "balanced";
    };
    return fundName + " - " + riskDesc + " investment option";
  }
  
  private static String generateCategory(String fundCode) {
    return switch (fundCode) {
      case "TAATTU_TUOTTO" -> "GUARANTEED";
      case "OBLIGAATIORAHASTO" -> "FIXED_INCOME";
      case "TASAPAINOINEN_RAHASTO" -> "BALANCED";
      case "OSAKERAHASTO", "INDEKSIRAHASTO" -> "EQUITY";
      case "KIINTEISTO_RAHASTO" -> "REAL_ESTATE";
      case "EUROOPPA_RAHASTO", "KEHITTYVAT_MARKKINAT" -> "REGIONAL";
      case "TEKNOLOGIA_RAHASTO", "TERVEYDENHUOLTO_RAHASTO" -> "SECTOR";
      case "VASTUULLINEN_SIJOITTAMINEN" -> "ESG";
      default -> "MIXED";
    };
  }
  
  private static LocalDate generateInceptionDate() {
    return LocalDate.now().minusYears(1 + RANDOM.nextInt(10)).minusDays(RANDOM.nextInt(365));
  }
  
  private static LocalDate calculateNextPaymentDate(LocalDate startDate) {
    LocalDate current = LocalDate.now();
    LocalDate nextPayment = startDate;
    
    while (nextPayment.isBefore(current)) {
      nextPayment = nextPayment.plusMonths(1);
    }
    
    return nextPayment;
  }
  
  private static String generateBankAccount() {
    // Generate Finnish IBAN using faker pattern or custom logic
    return "FI" + String.format("%02d", 10 + RANDOM.nextInt(90)) + 
           String.format("%014d", Math.abs(RANDOM.nextLong()) % 100000000000000L);
  }
  
  private static String determineCoverageType(String productCode) {
    return switch (productCode) {
      case "FEEMI_SAVINGS" -> "DEATH_BENEFIT";
      case "FEEMI_PENSION" -> "DISABILITY_BENEFIT";
      case "FEEMI_PS" -> "GOVERNMENT_BONUS";
      default -> "BASIC_COVERAGE";
    };
  }
  
  private static String generateCoverageCode(String coverageType) {
    return switch (coverageType) {
      case "DEATH_BENEFIT" -> "DEATH_" + (1000 + RANDOM.nextInt(4000));
      case "DISABILITY_BENEFIT" -> "DISABILITY_" + (500 + RANDOM.nextInt(2000));
      case "GOVERNMENT_BONUS" -> "GOV_BONUS_4_5_PCT";
      default -> "BASIC_" + (500 + RANDOM.nextInt(1500));
    };
  }
  
  private static BigDecimal generateSumInsured(String coverageType) {
    return switch (coverageType) {
      case "DEATH_BENEFIT" -> new BigDecimal(1000 + RANDOM.nextInt(4000));
      case "DISABILITY_BENEFIT" -> new BigDecimal(500 + RANDOM.nextInt(2000));
      case "GOVERNMENT_BONUS" -> new BigDecimal("0.045");
      default -> new BigDecimal(500 + RANDOM.nextInt(1500));
    };
  }
  
  private static BigDecimal calculatePremium(BigDecimal sumInsured, String coverageType) {
    return switch (coverageType) {
      case "DEATH_BENEFIT" -> sumInsured.multiply(new BigDecimal("0.001"));
      case "DISABILITY_BENEFIT" -> sumInsured.multiply(new BigDecimal("0.002"));
      case "GOVERNMENT_BONUS" -> BigDecimal.ZERO;
      default -> sumInsured.multiply(new BigDecimal("0.0015"));
    };
  }
}