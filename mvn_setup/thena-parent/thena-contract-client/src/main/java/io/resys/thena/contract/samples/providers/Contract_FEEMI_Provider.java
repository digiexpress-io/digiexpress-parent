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
import io.resys.thena.contract.samples.GenerationOptions;
import io.resys.thena.contract.samples.GenerationOptions.AgeRange;
import io.resys.thena.contract.samples.GenerationOptions.IncomeRange;
import io.resys.thena.contract.samples.contracts.FeemiContractVisitor;
import io.resys.thena.product.client.samples.Product_Feemi_Savings;

/**
 * Example demonstrating how to use FeemiContractVisitor to generate 
 * realistic Finnish insurance contracts and persist them to the database.
 */
public class Contract_FEEMI_Provider {

  /**
   * Generate and persist a Feemi Savings contract with default options
   */
  public static void newSavings(ContractClient contractClient, String tenantId) {
    // Create generation options
    GenerationOptions options = GenerationOptions.builder()
        .ageRange(AgeRange.of(25, 65))
        .incomeRange(IncomeRange.of(30000, 80000))
        .isIncludeBeneficiaries(true)
        .riskProfile("MODERATE")
        .build();
    
    // Get the Feemi Savings product
    final var product = Product_Feemi_Savings.create();
    
    // Generate and commit the contract
    contractClient.withTenant(tenantId).commit()
        .createOneContract()
        .contract(contract -> FeemiContractVisitor.visitSavingsContract(contract, product, options))
        .onNewContract(newState -> {
          System.out.println("Generated Feemi Savings Contract:");
          System.out.println("Contract ID: " + newState.getContract().getId());
          System.out.println("Contract Number: " + newState.getContract().getContractNumber());
          System.out.println("Parties: " + newState.getParties().size());
          System.out.println("Coverages: " + newState.getCoverages().size());
          System.out.println("Investment Plans: " + newState.getInvPlans().size());
        })
        .build();
  }
  
}