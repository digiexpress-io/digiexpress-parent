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
import io.resys.thena.contract.samples.GenerationOptions;
import io.resys.thena.contract.samples.GenerationOptions.IncomeRange;
import io.resys.thena.contract.samples.contracts.FeemiContractVisitor;
import io.resys.thena.contract.samples.products.ProductConstraintExtractor;
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
   * Generate and persist a Feemi Savings contract with default options
   */
  public static Uni<OneContractEnvelope> newSavings(ContractClient contractClient) {
    final var product = Product_Feemi_Savings.create();
    final var constraints = ProductConstraintExtractor.extractConstraints(product);
    
    GenerationOptions options = GenerationOptions.builder()
        .ageRange(constraints.ageRange)  // Extract age range from product rules
        .incomeRange(IncomeRange.of(30000, 80000))
        .isIncludeBeneficiaries(true)
        .riskProfile("MODERATE")
        .build();
    
    return contractClient.withTenant().commit()
        .createOneContract()
        .contract(contract -> FeemiContractVisitor.visitSavingsContract(contract, product, options))
        .onNewContract(newState -> {
          log.info("✅ Generated Feemi Savings Contract:");
          log.info("   📝 Contract ID: {}", newState.getContract().getId());
          log.info("   🔢 Contract Number: {}", newState.getContract().getContractNumber());
          log.info("   👥 Parties: {}", newState.getParties().size());
          log.info("   🛡️ Coverages: {}", newState.getCoverages().size());
          log.info("   💰 Investment Plans: {}", newState.getInvPlans().size());
        })
        .commitAuthor(Contract_Provider.class.getName())
        .commitMessage("Generated Feemi Savings contract")
        .build();
  }

  /**
   * Generate and persist a Feemi Pension contract with default options
   */
  public static Uni<OneContractEnvelope> newPension(ContractClient contractClient) {
    final var product = Product_Feemi_Pension.create();
    final var constraints = ProductConstraintExtractor.extractConstraints(product);
    
    GenerationOptions options = GenerationOptions.builder()
        .ageRange(constraints.ageRange)  // Extract age range from product rules
        .incomeRange(IncomeRange.of(35000, 90000))
        .isIncludeBeneficiaries(false)
        .riskProfile("CONSERVATIVE")
        .build();
    
    return contractClient.withTenant().commit()
        .createOneContract()
        .contract(contract -> FeemiContractVisitor.visitPensionContract(contract, product, options))
        .onNewContract(newState -> {
          log.info("✅ Generated Feemi Pension Contract:");
          log.info("   📝 Contract ID: {}", newState.getContract().getId());
          log.info("   🔢 Contract Number: {}", newState.getContract().getContractNumber());
          log.info("   👥 Parties: {}", newState.getParties().size());
          log.info("   🛡️ Coverages: {}", newState.getCoverages().size());
          log.info("   💳 Payment Plans: {}", newState.getPaymentPlans().size());
        })
        .commitAuthor(Contract_Provider.class.getName())
        .commitMessage("Generated Feemi Pension contract")
        .build();
  }

  /**
   * Generate and persist a Feemi PS contract with default options
   */
  public static Uni<OneContractEnvelope> newPS(ContractClient contractClient) {
    final var product = Product_Feemi_PS.create();
    final var constraints = ProductConstraintExtractor.extractConstraints(product);
    
    GenerationOptions options = GenerationOptions.builder()
        .ageRange(constraints.ageRange)  // Extract age range from product rules
        .incomeRange(IncomeRange.of(25000, 75000))
        .isIncludeBeneficiaries(false)
        .riskProfile("MODERATE")
        .build();
    
    return contractClient.withTenant().commit()
        .createOneContract()
        .contract(contract -> FeemiContractVisitor.visitPSContract(contract, product, options))
        .onNewContract(newState -> {
          log.info("✅ Generated Feemi PS Contract:");
          log.info("   📝 Contract ID: {}", newState.getContract().getId());
          log.info("   🔢 Contract Number: {}", newState.getContract().getContractNumber());
          log.info("   👥 Parties: {}", newState.getParties().size());
          log.info("   🛡️ Coverages: {}", newState.getCoverages().size());
          log.info("   💳 Payment Plans: {}", newState.getPaymentPlans().size());
          log.info("   🏛️ Government Bonus Notes: {}", newState.getNotes().size());
        })
        .commitAuthor(Contract_Provider.class.getName())
        .commitMessage("Generated Feemi PS contract")
        .build();
  }

  /**
   * Generate and persist a Nova Virtus contract with default options
   */
  public static Uni<OneContractEnvelope> newNovaVirtus(ContractClient contractClient) {
    final var product = Product_Nova_Virtus.create();
    final var constraints = ProductConstraintExtractor.extractConstraints(product);
    
    GenerationOptions options = GenerationOptions.builder()
        .ageRange(constraints.ageRange)  // Extract age range from product rules (fallback 18-70 for Nova Virtus)
        .incomeRange(IncomeRange.of(40000, 120000))
        .isIncludeBeneficiaries(true)
        .riskProfile("AGGRESSIVE")
        .build();
    
    return contractClient.withTenant().commit()
        .createOneContract()
        .contract(contract -> FeemiContractVisitor.visitNovaVirtusContract(contract, product, options))
        .onNewContract(newState -> {
          log.info("✅ Generated Nova Virtus Endowment Contract:");
          log.info("   📝 Contract ID: {}", newState.getContract().getId());
          log.info("   🔢 Contract Number: {}", newState.getContract().getContractNumber());
          log.info("   👥 Parties: {}", newState.getParties().size());
          log.info("   🛡️ Inheritance Coverage: {}", newState.getCoverages().size());
          log.info("   💰 Investment Plans (Granite/Globe/ETF): {}", newState.getInvPlans().size());
          log.info("   🔗 Product References: {}", newState.getReferences().size());
        })
        .commitAuthor(Contract_Provider.class.getName())
        .commitMessage("Generated Nova Virtus endowment contract")
        .build();
  }
}