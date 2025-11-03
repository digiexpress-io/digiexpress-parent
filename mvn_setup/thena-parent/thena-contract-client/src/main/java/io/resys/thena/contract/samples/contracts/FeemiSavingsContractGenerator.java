package io.resys.thena.contract.samples.contracts;

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

import io.resys.thena.contract.samples.GenerationOptions;
import io.resys.thena.contract.samples.GenerationOptions.AgeRange;
import io.resys.thena.contract.samples.GenerationOptions.IncomeRange;
import io.resys.thena.contract.samples.products.ProductConstraintExtractor;
import io.resys.thena.contract.samples.products.ProductConstraintExtractor.AllocationRules;
import io.resys.thena.contract.samples.products.ProductConstraintExtractor.InvestmentOption;
import io.resys.thena.contract.samples.products.ProductConstraintExtractor.ProductConstraints;
import io.resys.thena.contract.samples.providers.CRM_Provider;
import io.resys.thena.contract.samples.providers.Fund_Provider;
import io.resys.thena.product.client.api.Product;
import io.vertx.core.json.JsonObject;

public class FeemiSavingsContractGenerator {
  
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
    public String contractNumber;
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
  
  public static class PaymentPlanData {
    public BigDecimal monthlyAmount;
    public String frequency;
    public LocalDate startDate;
  }
  
  public static class InvestmentAllocationData {
    public String fundCode;
    public String fundName;
    public BigDecimal percentage;
    public String riskLevel;
  }
  
  public static class CoverageData {
    public String coverageType;
    public String coverageCode;
    public BigDecimal sumInsured;
  }
  
  public static GeneratedContractData generate(Product product, GenerationOptions options) {
    // Extract constraints from product rules
    ProductConstraints constraints = ProductConstraintExtractor.extractConstraints(product);
    
    GeneratedContractData data = new GeneratedContractData();
    
    // Generate contract data
    data.contract = generateContractData(product, constraints);
    
    // Generate policyholder respecting age and income constraints
    data.policyholder = generatePolicyholder(constraints, options);
    
    // Generate beneficiary
    if (options.isIncludeBeneficiaries()) {
      data.beneficiary = generateBeneficiary();
    }
    
    // Generate payment plan based on income and product constraints
    data.paymentPlan = generatePaymentPlan(data.policyholder.annualIncome, constraints);
    
    // Generate investment allocations based on risk profile and product options
    data.investmentAllocations = generateInvestmentAllocations(
        constraints.investmentOptions, 
        constraints.allocationRules, 
        options.getRiskProfile());
    
    // Generate coverage
    data.coverage = generateCoverage(product.getProductCode());
    
    return data;
  }
  
  private static ContractData generateContractData(Product product, ProductConstraints constraints) {
    ContractData contract = new ContractData();
    
    contract.productCode = product.getProductCode();
    contract.contractNumber = "SAV-" + String.format("%08d", RANDOM.nextInt(99999999));
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
  
  private static PolicyholderData generatePolicyholder(ProductConstraints constraints, GenerationOptions options) {
    PolicyholderData holder = new PolicyholderData();
    
    // Determine age range (respect both product constraints and generation options)
    AgeRange ageRange = constraints.ageRange;
    if (options.getAgeRange().isPresent()) {
      AgeRange optionsAge = options.getAgeRange().get();
      int minAge = Math.max(ageRange.getMinAge(), optionsAge.getMinAge());
      int maxAge = Math.min(ageRange.getMaxAge(), optionsAge.getMaxAge());
      ageRange = AgeRange.of(minAge, maxAge);
    }
    
    // Determine income range (respect both product constraints and generation options)
    IncomeRange incomeRange = constraints.contributionRange;
    if (options.getIncomeRange().isPresent()) {
      IncomeRange optionsIncome = options.getIncomeRange().get();
      int minIncome = Math.max(incomeRange.getMinIncome(), optionsIncome.getMinIncome());
      int maxIncome = Math.min(incomeRange.getMaxIncome(), optionsIncome.getMaxIncome());
      incomeRange = IncomeRange.of(minIncome, maxIncome);
    }
    
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
  
  private static PaymentPlanData generatePaymentPlan(int annualIncome, ProductConstraints constraints) {
    PaymentPlanData payment = new PaymentPlanData();
    
    // Generate payment plan using Fund_Provider
    Fund_Provider.PaymentPlan fundPayment = Fund_Provider.generatePaymentPlan(annualIncome);
    
    // Map to PaymentPlanData
    payment.monthlyAmount = fundPayment.getMonthlyAmount();
    payment.frequency = fundPayment.getFrequency();
    payment.startDate = fundPayment.getStartDate();
    
    return payment;
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
  
  private static CoverageData generateCoverage(String productCode) {
    CoverageData coverage = new CoverageData();
    
    // Generate coverage using Fund_Provider
    Fund_Provider.Coverage fundCoverage = Fund_Provider.generateCoverage(productCode);
    
    // Map to CoverageData
    coverage.coverageType = fundCoverage.getCoverageType();
    coverage.coverageCode = fundCoverage.getCoverageCode();
    coverage.sumInsured = fundCoverage.getSumInsured();
    
    return coverage;
  }
}