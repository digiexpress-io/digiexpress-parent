package io.resys.thena.contract.samples.providers;

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

import io.resys.thena.contract.client.api.ContractClient;
import io.resys.thena.contract.client.api.ContractCommitActions.OneContractEnvelope;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.samples.GenerationOptions;
import io.resys.thena.contract.samples.contracts.SampleContractVisitor;
import io.resys.thena.product.client.api.Product;
import io.resys.thena.product.client.api.Product.AgeRange;
import io.resys.thena.product.client.api.Product.IncomeRange;
import io.resys.thena.product.client.samples.Product_Feemi_PS;
import io.resys.thena.product.client.samples.Product_Feemi_Pension;
import io.resys.thena.product.client.samples.Product_Feemi_Savings;
import io.resys.thena.product.client.samples.Product_Nova_Virtus;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

/**
 * Central provider for generating realistic Finnish insurance contracts 
 * for all available products and persisting them to the database.
 */
@Slf4j
public class Contract_Provider {

  /**
   * Generate and persist a Feemi Savings contract using full product capabilities
   */
  public static Uni<OneContractEnvelope> newSavings(ContractClient contractClient, String refNumber) {
    final var product = Product_Feemi_Savings.create();
    
    GenerationOptions options = GenerationOptions.builder()
        .refNumber(refNumber)
        .ageRange(product.getAgeRange().orElse(AgeRange.of(18, 70)))
        .incomeRange(product.getContributionRange().orElse(IncomeRange.of(30000, 80000)))
        .isIncludeBeneficiaries(hasDeathBenefits(product))  // Auto-detect if product has death coverage
        .riskProfile(determineRiskProfile(product))  // Derive risk profile from investment options
        .build();
    
    return contractClient.withTenant().commit()
        .createOneContract()
        .contract(contract -> SampleContractVisitor.visitSavingsContract(contract, product, options))
        .onNewContract(newState -> {
          logProductBasedContract("Feemi Savings", product, newState);
        })
        .commitAuthor(Contract_Provider.class.getName())
        .commitMessage("Generated Feemi Savings contract")
        .build();
  }

  /**
   * Generate and persist a Feemi Pension contract using full product capabilities
   */
  public static Uni<OneContractEnvelope> newPension(ContractClient contractClient) {
    final var product = Product_Feemi_Pension.create();
    
    GenerationOptions options = GenerationOptions.builder()
        .ageRange(product.getAgeRange().orElse(AgeRange.of(18, 70)))
        .incomeRange(product.getContributionRange().orElse(IncomeRange.of(35000, 90000)))
        .isIncludeBeneficiaries(hasDeathBenefits(product))
        .riskProfile(determineRiskProfile(product))
        .build();
    
    return contractClient.withTenant().commit()
        .createOneContract()
        .contract(contract -> SampleContractVisitor.visitPensionContract(contract, product, options))
        .onNewContract(newState -> {
          logProductBasedContract("Feemi Pension", product, newState);
        })
        .commitAuthor(Contract_Provider.class.getName())
        .commitMessage("Generated Feemi Pension contract")
        .build();
  }

  /**
   * Generate and persist a Feemi PS contract using full product capabilities
   */
  public static Uni<OneContractEnvelope> newPS(ContractClient contractClient) {
    final var product = Product_Feemi_PS.create();
    
    GenerationOptions options = GenerationOptions.builder()
        .ageRange(product.getAgeRange().orElse(AgeRange.of(18, 70)))
        .incomeRange(product.getContributionRange().orElse(IncomeRange.of(25000, 75000)))
        .isIncludeBeneficiaries(hasDeathBenefits(product))
        .riskProfile(determineRiskProfile(product))
        .build();
    
    return contractClient.withTenant().commit()
        .createOneContract()
        .contract(contract -> SampleContractVisitor.visitPSContract(contract, product, options))
        .onNewContract(newState -> {
          logProductBasedContract("Feemi PS", product, newState);
        })
        .commitAuthor(Contract_Provider.class.getName())
        .commitMessage("Generated Feemi PS contract")
        .build();
  }

  /**
   * Generate and persist a Nova Virtus contract using full product capabilities
   */
  public static Uni<OneContractEnvelope> newNovaVirtus(ContractClient contractClient) {
    final var product = Product_Nova_Virtus.create();
    
    GenerationOptions options = GenerationOptions.builder()
        .ageRange(product.getAgeRange().orElse(AgeRange.of(18, 70)))
        .incomeRange(product.getContributionRange().orElse(IncomeRange.of(40000, 120000)))
        .isIncludeBeneficiaries(hasDeathBenefits(product))
        .riskProfile(determineRiskProfile(product))
        .build();
    
    return contractClient.withTenant().commit()
        .createOneContract()
        .contract(contract -> SampleContractVisitor.visitNovaVirtusContract(contract, product, options))
        .onNewContract(newState -> {
          logProductBasedContract("Nova Virtus Endowment", product, newState);
        })
        .commitAuthor(Contract_Provider.class.getName())
        .commitMessage("Generated Nova Virtus endowment contract")
        .build();
  }

  /**
   * Determines if the product has death benefits by checking cover options
   */
  private static boolean hasDeathBenefits(Product product) {
    return !product.getCoverOptions().isEmpty();
  }

  /**
   * Determines risk profile based on product's investment options
   */
  private static String determineRiskProfile(Product product) {
    final var investmentOptions = product.getInvestmentOptions();
    
    // Count risk levels
    final long conservativeCount = investmentOptions.stream()
        .filter(option -> "none".equalsIgnoreCase(option.getRiskLevel()) || 
                         "low".equalsIgnoreCase(option.getRiskLevel()))
        .count();
    
    final long aggressiveCount = investmentOptions.stream()
        .filter(option -> "high".equalsIgnoreCase(option.getRiskLevel()) || 
                         option.getName().toLowerCase().contains("equity") ||
                         option.getName().toLowerCase().contains("etf"))
        .count();
    
    // Determine profile based on investment mix
    if (conservativeCount > 0 && aggressiveCount == 0) return "CONSERVATIVE";
    if (aggressiveCount > conservativeCount) return "AGGRESSIVE";
    return "MODERATE";
  }

  /**
   * Enhanced logging that shows product capabilities and contract results
   */
  private static void logProductBasedContract(String productName, Product product, ContractContainer container) {
    final var coverOptions = product.getCoverOptions();
    final var investmentOptions = product.getInvestmentOptions();
    final var ageRange = product.getAgeRange();
    
    log.info("✅ Generated {} Contract:", productName);
    log.info("   📝 Contract ID: {}", container.getContract().getId());
    log.info("   🔢 Contract Number: {}", container.getContract().getContractNumber());
    log.info("   👥 Parties: {}", container.getParties().size());
    
    // Show coverage based on product capabilities
    if (!coverOptions.isEmpty()) {
      log.info("   💀 Death Benefits: {} ({})", container.getCoverages().size(), 
          coverOptions.stream().map(c -> c.getCoverType()).distinct().count() + " types");
    } else {
      log.info("   🛡️ Coverages: {} (No death benefits)", container.getCoverages().size());
    }
    
    // Show investment details
    log.info("   💰 Investment Plans: {} ({} options available)", 
        container.getInvPlans().size(), investmentOptions.size());
    
    // Show product constraints applied
    if (ageRange.isPresent()) {
      log.info("   👶 Age Range: {}-{} years", ageRange.get().getMinAge(), ageRange.get().getMaxAge());
    }
    
    log.info("   🔗 References: {}", container.getReferences().size());
    log.info("   📋 Notes: {}", container.getNotes().size());
  }
}