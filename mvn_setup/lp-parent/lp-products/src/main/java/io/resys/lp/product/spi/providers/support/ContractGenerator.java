package io.resys.lp.product.spi.providers.support;

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
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.lp.product.spi.providers.CRM_Provider;
import io.resys.lp.product.spi.providers.Fund_Provider;
import io.resys.lp.product.spi.providers.GenerationOptions;
import io.resys.thena.product.client.api.Product;
import io.resys.thena.product.client.api.Product.AgeRange;
import io.resys.thena.product.client.api.Product.AllocationRules;
import io.resys.thena.product.client.api.Product.IncomeRange;
import io.resys.thena.product.client.api.Product.InvestmentOption;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

public class ContractGenerator {
  
  private static final Random RANDOM = new Random();
  
  public static class GeneratedContractData {
    public ContractData contract;
    public PolicyholderData policyholder;
    public BeneficiaryData beneficiary;
    public PaymentPlanData paymentPlan;
    public List<InvestmentAllocationData> investmentAllocations;
    public CoverageData coverage;
    
    public GeneratedContractData() {
      this.investmentAllocations = new ArrayList<>();
    }
  }
  
  public static class ContractData {
    public String productCode;
    //public String contractNumber;
    public LocalDate issueDate;
    public LocalDate startDate;
    public JsonObject contractData;
  }
  
  public static class PolicyholderData {
    public String personalId;
    public String fullName;
    public int age;
    public int annualIncome;
    public String address;
    public String city;
    public String postalCode;
    public String phone;
    public String email;
    public String employmentStatus;
    public String employer;
    public String iban;
    public JsonObject partyData;
  }
  
  public static class BeneficiaryData {
    public String personalId;
    public String fullName;
    public String relationship;
    public int percentage;
    public JsonObject partyData;
  }
  
  public static class InvestmentAllocationData {
    public String fundCode;
    public String fundName;
    public BigDecimal percentage;
    public String riskLevel;
  }
  

  @Data
  @Builder
  @Jacksonized
  public static class PaymentPlanData {
    private BigDecimal monthlyAmount;
    private BigDecimal annualAmount;
    private String frequency;
    private LocalDate startDate;
    private LocalDate nextPaymentDate;
    private String paymentMethod;
    private String bankAccount;
    private boolean active;
    
    @JsonIgnore
    public JsonObject toJson() {
      String json = new JsonObject(toMap()).encode();
      return new JsonObject(json);
    }
    
    @JsonIgnore
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
  public static class CoverageData {
    private String coverageType;
    private String coverageCode;
    private BigDecimal sumInsured;
    private BigDecimal premium;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private boolean active;
    
    
    @JsonIgnore
    public JsonObject toJson() {
      String json = new JsonObject(toMap()).encode();
      return new JsonObject(json);
    }
    
    @JsonIgnore
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
  
  
  
  public static GeneratedContractData generate(Product product, GenerationOptions options) {
    GeneratedContractData data = new GeneratedContractData();
    
    // Generate contract data
    data.contract = generateContractData(product, product);
    
    // Generate policyholder respecting age and income constraints
    data.policyholder = generatePolicyholder(product, options);
    
    // Generate beneficiary
    if (options.isIncludeBeneficiaries()) {
      data.beneficiary = generateBeneficiary();
    }
    
    // Generate payment plan based on income and product constraints
    data.paymentPlan = generatePaymentPlan(data.policyholder.annualIncome, product);
    
    // Generate investment allocations based on risk profile and product options
    data.investmentAllocations = generateInvestmentAllocations(
        product.getInvestmentOptions(), 
        product.getAllocationRules(), 
        options.getRiskProfile());
    
    // Generate coverage
    data.coverage = generateCoverage(product.getProductCode());
    
    return data;
  }
  
  private static ContractData generateContractData(Product product, Product constraints) {
    ContractData contract = new ContractData();
    
    contract.productCode = product.getProductCode();
    // contract.contractNumber = "SAV-" + String.format("%08d", RANDOM.nextInt(99999999));
    contract.issueDate = LocalDate.now().minusDays(RANDOM.nextInt(730)); // Up to 2 years ago
    contract.startDate = contract.issueDate.plusDays(1);
    
    // Build contract data JSON
    JsonObject contractData = new JsonObject();
    contractData.put("productCode", product.getProductCode());
    contractData.put("productName", product.getProductName());
    contractData.put("category", product.getCategory());
    contractData.put("issueDate", contract.issueDate.toString());
    
    Map<String, Object> limits = new HashMap<>();
    limits.put("annualMaxContribution", 60000);
    limits.put("contractMinValue", 1000);
    limits.put("partialWithdrawalMin", 500);
    contractData.put("limits", limits);
    
    contract.contractData = contractData;
    return contract;
  }
  
  private static PolicyholderData generatePolicyholder(Product constraints, GenerationOptions options) {
    PolicyholderData holder = new PolicyholderData();
    
    // Determine age range (respect both product constraints and generation options)
    AgeRange ageRange = options.getAgeRange();
    
    // Determine income range (respect both product constraints and generation options)
    IncomeRange incomeRange = options.getIncomeRange();
    
    // Generate person using CRM_Provider
    int targetAge = ageRange.getMinAge() + RANDOM.nextInt(ageRange.getMaxAge() - ageRange.getMinAge() + 1);
    CRM_Provider.Person person = CRM_Provider.generatePerson(targetAge, incomeRange.getMinIncome(), incomeRange.getMaxIncome());
    
    // Map to PolicyholderData
    holder.personalId = person.getPersonalId();
    holder.fullName = person.getFullName();
    holder.age = person.getAge();
    holder.annualIncome = person.getEmployment().getAnnualIncome();
    holder.address = person.getAddress().getStreet();
    holder.city = person.getAddress().getCity();
    holder.postalCode = person.getAddress().getPostalCode();
    holder.phone = person.getContact().getPhone();
    holder.email = person.getContact().getEmail();
    holder.employmentStatus = person.getEmployment().getStatus();
    holder.employer = person.getEmployment().getEmployer();
    holder.iban = person.getBanking().getIban();
    holder.partyData = person.toJson();
    
    return holder;
  }
  
  private static BeneficiaryData generateBeneficiary() {
    BeneficiaryData beneficiary = new BeneficiaryData();
    
    // Generate beneficiary using CRM_Provider
    CRM_Provider.Beneficiary crmBeneficiary = CRM_Provider.generateBeneficiary("SPOUSE", 100);
    
    // Map to BeneficiaryData
    beneficiary.personalId = crmBeneficiary.getPersonalId();
    beneficiary.fullName = crmBeneficiary.getFullName();
    beneficiary.relationship = crmBeneficiary.getRelationship();
    beneficiary.percentage = crmBeneficiary.getPercentage();
    beneficiary.partyData = crmBeneficiary.toJson();
    
    return beneficiary;
  }
  
  private static List<InvestmentAllocationData> generateInvestmentAllocations(
      List<InvestmentOption> options, AllocationRules rules, String riskProfile) {
    
    List<InvestmentAllocationData> allocations = new ArrayList<>();
    
    // Generate allocations using Fund_Provider
    List<Fund_Provider.InvestmentAllocation> fundAllocations = Fund_Provider.generateAllocations(riskProfile, new BigDecimal("10000"));
    
    // Map to InvestmentAllocationData
    for (Fund_Provider.InvestmentAllocation fundAllocation : fundAllocations) {
      InvestmentAllocationData allocation = new InvestmentAllocationData();
      allocation.fundCode = fundAllocation.getFundCode();
      allocation.fundName = fundAllocation.getFundName();
      allocation.percentage = fundAllocation.getPercentage();
      allocation.riskLevel = fundAllocation.getRiskLevel();
      allocations.add(allocation);
    }
    
    return allocations;
  }


  public static PaymentPlanData generatePaymentPlan(int annualIncome, Product constraints) {
    double contributionRate = 0.03 + (RANDOM.nextDouble() * 0.05);
    int monthlyAmount = (int) (annualIncome * contributionRate / 12);
    monthlyAmount = Math.round(monthlyAmount / 25.0f) * 25;
    monthlyAmount = Math.max(50, Math.min(5000, monthlyAmount));
    
    LocalDate startDate = LocalDate.now().minusDays(RANDOM.nextInt(365));
    
    return PaymentPlanData.builder()
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
  
  public static CoverageData generateCoverage(String productCode) {
    String coverageType = determineCoverageType(productCode);
    BigDecimal sumInsured = generateSumInsured(coverageType);
    LocalDate effectiveDate = LocalDate.now().minusDays(RANDOM.nextInt(730));
    
    return CoverageData.builder()
        .coverageType(coverageType)
        .coverageCode(generateCoverageCode(coverageType))
        .sumInsured(sumInsured)
        .premium(calculatePremium(sumInsured, coverageType))
        .effectiveDate(effectiveDate)
        .expiryDate(effectiveDate.plusYears(1))
        .active(true)
        .build();
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