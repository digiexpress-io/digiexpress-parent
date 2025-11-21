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

import java.time.LocalDate;

import org.apache.commons.lang3.mutable.MutableObject;

import io.resys.lp.product.spi.providers.CRM_Provider;
import io.resys.lp.product.spi.providers.Fund_Provider;
import io.resys.lp.product.spi.providers.GenerationOptions;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewContract;
import io.resys.thena.contract.client.entities.ContractEntity.ContractRelationType;
import io.resys.thena.contract.client.entities.ImmutableContractOneOfRelations;
import io.resys.thena.contract.client.entities.Party;
import io.resys.thena.product.client.api.Product;
import io.vertx.core.json.JsonObject;

/**
 * Feemi contract visitor that implements the NewContract interface and generates
 * realistic Finnish insurance contract data using CRM_Provider and Fund_Provider.
 */
public class SampleContractVisitor {
  
  public static void visitSavingsContract(NewContract newContract, Product product, GenerationOptions options) {
    
    // Generate contract data using FeemiSavingsContractGenerator
    SavingsContractGenerator.GeneratedContractData contractData = 
        SavingsContractGenerator.generate(product, options);
    
    
    final var policyholder = new MutableObject<Party>();
    
    // Build the contract using the NewContract interface
    newContract
        
        .contractNumber(options.getRefNumber())
        .contractIssueDate(contractData.contract.issueDate)        
        
        .contractStartDate(contractData.contract.startDate)
        
        .contractStatus("ACTIVE")
        .contractType("SAVINGS_INSURANCE")
        .contractSubType("FEEMI_SAVINGS")
        .contractData(contractData.contract.contractData)
        
        .addNote(newNote -> {
          newNote
            .noteType("product-description")
            .noteValue("json-format")
            .noteBody(JsonObject.mapFrom(product))
            .build();
        })
        
        
        // Add policyholder as primary party
        .addParty(party -> {
          final var built = party
              .externalId(contractData.policyholder.personalId)
              .partyType("POLICYHOLDER")
              .partyEffectiveFrom(contractData.contract.startDate)
              .partyTermStartDate(contractData.contract.startDate)              
              .partyData(contractData.policyholder.partyData)
              .build();
          
          policyholder.setValue(built);
        })
        
        // Add beneficiary if present
        .addParty(party -> {
          if (contractData.beneficiary != null) {
            party
                .externalId(contractData.beneficiary.personalId)
                .partyType("BENEFICIARY")
                .partyEffectiveFrom(contractData.contract.startDate)
                .partyTermStartDate(contractData.contract.startDate)
                .partyData(contractData.beneficiary.partyData)
                .build();
          }
        })
        
        // Add coverage
        .addCoverage(coverage -> {
          coverage
              .insuredId(policyholder.get().getId())
              .externalId(contractData.coverage.coverageCode)
              .coverageType(contractData.coverage.coverageType)
              .coverageCode(contractData.coverage.coverageCode)
              .coverageSumInsured(contractData.coverage.sumInsured)
              .coverageStatus("ACTIVE")
              .coverageEffectiveFrom(contractData.contract.startDate)
              .coverageTermStartDate(contractData.contract.startDate)
              .build();
        })
        
        // Add payment plan
        .addPaymentPlan(paymentPlan -> {
          paymentPlan
              .paymentPlanStatus("ACTIVE")
              .paymentPlanFrequency(contractData.paymentPlan.frequency)
              .paymentPlanAmount(contractData.paymentPlan.monthlyAmount)
              .paymentPlanStartDate(contractData.paymentPlan.startDate)
              .build();
        })
        
        // Add investment plan with allocations
        .addInvPlan(invPlan -> {
          invPlan
              .invPlanRefNumber("INV_PLAN_" + options.getRefNumber())
              .invPlanCode("FEEMI_SAVINGS_PLAN")
              .invPlanName("Feemi Savings Investment Plan")
              .invPlanStatus("ACTIVE")
              .invPlanStartDate(contractData.contract.startDate)
              .build();
          
          // Add allocations
          for (SavingsContractGenerator.InvestmentAllocationData allocation : contractData.investmentAllocations) {
            invPlan.addAllocation(alloc -> {
              alloc
                  .invPlanAllocCode(allocation.fundCode)
                  .invPlanAllocName(allocation.fundName)
                  .invPlanAllocPercentage(allocation.percentage)
                  .invPlanAllocStatus("ACTIVE")
                  .build();
            });
          }
        })
        
        // Add product reference
        .addReference(reference -> {
          reference
              .referenceType("PRODUCT_CODE")
              .referenceValue(product.getProductCode())
              .build();
        })
        
        // Add generation note
        .addNote(note -> {
          note
              .noteType("GENERATION_INFO")
              .noteValue("Generated using FeemiContractVisitor with realistic Finnish demographic data")
              .build();
        })
        .build();
  }
  
  public static void visitPensionContract(NewContract newContract, Product product, GenerationOptions options) {
    
    // Generate person data
    CRM_Provider.Person person = CRM_Provider.generatePerson(
        options.getAgeRange().getMinAge(),
        options.getIncomeRange().getMinIncome(),
        options.getIncomeRange().getMaxIncome()
    );
    
    // Generate fund data
    Fund_Provider.PaymentPlan paymentPlan = Fund_Provider.generatePaymentPlan(person.getEmployment().getAnnualIncome());
    Fund_Provider.Coverage coverage = Fund_Provider.generateCoverage("FEEMI_PENSION");
    
    // Build pension contract
    final var startDate = LocalDate.now().minusDays((long) (Math.random() * 30));
    final var policyholder = new MutableObject<Party>();
    
    
    newContract
        .contractIssueDate(LocalDate.now().minusDays((long) (Math.random() * 365)))     
        .contractStartDate(startDate)
        
        .contractStatus("ACTIVE")
        .contractType("PENSION_INSURANCE")
        .contractSubType("FEEMI_PENSION")
        .contractNumber(options.getRefNumber())   
        
        // Add policyholder
        .addParty(party -> {
          final var built = party
              .externalId(person.getPersonalId())
              .partyType("POLICYHOLDER")
              .partyEffectiveFrom(startDate)
              .partyTermStartDate(startDate)
              .partyData(person.toJson())
              .build();
          policyholder.setValue(built);
        })
        
        // Add disability coverage
        .addCoverage(coverageBuilder -> {
          coverageBuilder
              .insuredId(policyholder.get().getId())
              .externalId(coverage.getCoverageCode())
              .coverageType(coverage.getCoverageType())
              .coverageCode(coverage.getCoverageCode())
              .coverageSumInsured(coverage.getSumInsured())
              .coverageStatus("ACTIVE")
              .coverageTermStartDate(startDate)
              .coverageEffectiveFrom(startDate)
              .build();
        })
        
        // Add payment plan
        .addPaymentPlan(paymentPlanBuilder -> {
          paymentPlanBuilder
              .paymentPlanStatus("ACTIVE")
              .paymentPlanFrequency(paymentPlan.getFrequency())
              .paymentPlanAmount(paymentPlan.getMonthlyAmount())
              .paymentPlanStartDate(startDate)
              .build();
        })
        
        // Add product reference
        .addReference(reference -> {
          reference
              .referenceType("PRODUCT_CODE")
              .referenceValue(product.getProductCode())
              .build();
        }).build();
  }
  
  public static void visitPSContract(NewContract newContract, Product product, GenerationOptions options) {
    
    // Generate person data
    CRM_Provider.Person person = CRM_Provider.generatePerson(
        options.getAgeRange().getMinAge(),
        options.getIncomeRange().getMinIncome(),
        options.getIncomeRange().getMaxIncome()
    );
    
    
    // Generate fund data
    Fund_Provider.PaymentPlan paymentPlan = Fund_Provider.generatePaymentPlan(person.getEmployment().getAnnualIncome());
    Fund_Provider.Coverage coverage = Fund_Provider.generateCoverage("FEEMI_PS");
    
    
    final var startDate = LocalDate.now().minusDays((long) (Math.random() * 30));
    final var policyholder = new MutableObject<Party>();
    
    
    // Build PS contract (with government bonus)
    newContract
        .contractNumber(options.getRefNumber())
        .contractIssueDate(LocalDate.now().minusDays((long) (Math.random() * 365)))
        
        .contractStartDate(startDate)
        .contractMaturityDate(LocalDate.now().plusYears(10)) // 10-year commitment
        .contractStatus("ACTIVE")
        .contractType("PS_INSURANCE")
        .contractSubType("FEEMI_PS")
        
        // Add policyholder
        .addParty(party -> {
          final var built = party
              .externalId(person.getPersonalId())
              .partyType("POLICYHOLDER")
              .partyEffectiveFrom(startDate)
              .partyTermStartDate(startDate)
              .partyData(person.toJson())
              .build();
          
          policyholder.setValue(built);
        })
        
        // Add government bonus coverage
        .addCoverage(coverageBuilder -> {
          coverageBuilder
              .insuredId(policyholder.get().getId())
              .externalId(coverage.getCoverageCode())
              .coverageType(coverage.getCoverageType())
              .coverageCode(coverage.getCoverageCode())
              .coverageSumInsured(coverage.getSumInsured()) // 4.5% bonus
              .coverageStatus("ACTIVE")
              .coverageEffectiveFrom(startDate)
              .coverageTermStartDate(startDate)
              .coverageTermEndDate(LocalDate.now().plusYears(10))
              .build();
        })
        
        // Add payment plan
        .addPaymentPlan(paymentPlanBuilder -> {
          paymentPlanBuilder
              .paymentPlanStatus("ACTIVE")
              .paymentPlanFrequency(paymentPlan.getFrequency())
              .paymentPlanAmount(paymentPlan.getMonthlyAmount())
              .paymentPlanStartDate(paymentPlan.getStartDate())
              .build();
        })
        
        // Add product reference
        .addReference(reference -> {
          reference
              .referenceType("PRODUCT_CODE")
              .referenceValue(product.getProductCode())
              .build();
        })
        
        // Add PS-specific note
        .addNote(note -> {
          note
              .noteType("GOVERNMENT_BONUS")
              .noteValue("Eligible for 4.5% annual government bonus with 10-year commitment")
              .build();
        }).build();
  }
  
  public static void visitNovaVirtusContract(NewContract newContract, Product product, GenerationOptions options) {
    
    // Generate person data
    CRM_Provider.Person person = CRM_Provider.generatePerson(
        options.getAgeRange().getMinAge(),
        options.getIncomeRange().getMinIncome(),
        options.getIncomeRange().getMaxIncome()
    );
    

    
    // Generate fund data
    Fund_Provider.PaymentPlan paymentPlan = Fund_Provider.generatePaymentPlan(person.getEmployment().getAnnualIncome());
    Fund_Provider.Coverage coverage = Fund_Provider.generateCoverage("NOVA_VIR_001");
    
    final var policyholder = new MutableObject<Party>();
    
    
    // Build Nova Virtus endowment contract
    newContract
        .contractNumber(options.getRefNumber())
        .contractIssueDate(LocalDate.now().minusDays((long) (Math.random() * 365)))
        .contractStartDate(LocalDate.now().minusDays((long) (Math.random() * 30)))
        
        .contractStatus("ACTIVE")
        .contractType("ENDOWMENT_INSURANCE")
        .contractSubType("NOVA_VIRTUS")
        .addNote(newNote -> {
          newNote
            .noteType("product-description")
            .noteValue("json-format")
            .noteBody(JsonObject.mapFrom(product))
            .build();
        })
        
        // Add policyholder
        .addParty(party -> {
          final var built = party
              .externalId(person.getPersonalId())
              .partyType("POLICYHOLDER")
              .partyEffectiveFrom(LocalDate.now())
              .partyTermStartDate(LocalDate.now())
              .partyData(person.toJson())
              .build();
          
          policyholder.setValue(built);
        })
        
        
        // Add inheritance cover
        .addCoverage(coverageBuilder -> {
          coverageBuilder
              .insuredId(policyholder.get().getId())
              .externalId(coverage.getCoverageCode())
              .coverageType("INHERITANCE_COVER")
              .coverageCode(coverage.getCoverageCode())
              .coverageSumInsured(coverage.getSumInsured())
              .coverageStatus("ACTIVE")
              .coverageEffectiveFrom(LocalDate.now())
              .coverageTermStartDate(LocalDate.now())
              .build();
        })
        
        // Add payment plan
        .addPaymentPlan(paymentPlanBuilder -> {
          paymentPlanBuilder
              .paymentPlanStatus("ACTIVE")
              .paymentPlanFrequency(paymentPlan.getFrequency())
              .paymentPlanAmount(paymentPlan.getMonthlyAmount())
              .paymentPlanStartDate(paymentPlan.getStartDate())
              .build();
        })
        
        // Add investment plan with Nova Virtus specific options
        .addInvPlan(invPlan -> {
          invPlan
              .invPlanRefNumber("NOVA_VIRTUS_PLAN_" + options.getRefNumber())
              .invPlanCode("NOVA_VIRTUS_PLAN")
              .invPlanName("Nova Virtus Investment Plan")
              .invPlanStatus("ACTIVE")
              .invPlanStartDate(LocalDate.now())
              .build();
          
          // Add Nova Virtus specific allocations (Granite/Globe portfolios, ETFs)
          invPlan.addAllocation(alloc -> {
            alloc
                .invPlanAllocCode("GRANITE_PORTFOLIO")
                .invPlanAllocName("Granite Portfolio - Diversified Real Estate Enhanced")
                .invPlanAllocPercentage(new java.math.BigDecimal("0.40"))
                .invPlanAllocStatus("ACTIVE")
                .build();
          });
          
          invPlan.addAllocation(alloc -> {
            alloc
                .invPlanAllocCode("GLOBE_BASKET_ESG")
                .invPlanAllocName("Globe Basket - ESG Focused")
                .invPlanAllocPercentage(new java.math.BigDecimal("0.35"))
                .invPlanAllocStatus("ACTIVE")
                .build();
          });
          
          invPlan.addAllocation(alloc -> {
            alloc
                .invPlanAllocCode("ETF_SELECTION")
                .invPlanAllocName("ETF Selection - Monthly Savings")
                .invPlanAllocPercentage(new java.math.BigDecimal("0.25"))
                .invPlanAllocStatus("ACTIVE")
                .build();
          });
        })
        
        // Add product reference
        .addReference(reference -> {
          reference
              .referenceType("PRODUCT_CODE")
              .referenceValue(product.getProductCode())
              .build();
        })
        
        // Add inheritance planning note
        .addNote(note -> {
          note
              .noteType("INHERITANCE_PLANNING")
              .noteValue("Unit-linked endowment with flexible inheritance planning and tax-efficient wealth transfer")
              .build();
        });
        
        // Add beneficiary clause note
  
        if(options.isIncludeBeneficiaries()) {
          final var beneficiary = new MutableObject<Party>();
          newContract.addParty(party -> {
            
            // Generate beneficiary if enabled
            final var template = CRM_Provider.generateBeneficiary("SPOUSE", 100);
    
            final var created = party
                .externalId(template.getPersonalId())
                .partyType("BENEFICIARY")
                .partyEffectiveFrom(LocalDate.now())
                .partyTermStartDate(LocalDate.now())
                .partyData(template.toJson())
                .build();
            
            beneficiary.setValue(created);
            
            
          })
          .addNote(note -> {
            note
                .relations(ImmutableContractOneOfRelations.builder()
                    .partyId(beneficiary.get().getId())
                    .relationType(ContractRelationType.PARTY)
                    .build())
                .noteType("BENEFICIARY_CLAUSE")
                .noteValue("Beneficiary clause allows distribution without separate will, marital rights can be excluded")
                .build();
          });
      }
      newContract.build();
        
  }
}
