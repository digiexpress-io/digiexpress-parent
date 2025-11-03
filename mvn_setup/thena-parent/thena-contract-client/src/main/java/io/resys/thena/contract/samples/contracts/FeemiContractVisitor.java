package io.resys.thena.contract.samples.contracts;

import java.time.LocalDate;

import io.resys.thena.contract.client.api.ThenaContractNewObject.NewContract;
import io.resys.thena.contract.samples.GenerationOptions;
import io.resys.thena.contract.samples.products.ProductConstraintExtractor;
import io.resys.thena.contract.samples.products.ProductConstraintExtractor.ProductConstraints;
import io.resys.thena.contract.samples.providers.CRM_Provider;
import io.resys.thena.contract.samples.providers.Fund_Provider;
import io.resys.thena.product.client.api.Product;

/**
 * Feemi contract visitor that implements the NewContract interface and generates
 * realistic Finnish insurance contract data using CRM_Provider and Fund_Provider.
 */
public class FeemiContractVisitor {
  
  public static void visitSavingsContract(NewContract newContract, Product product, GenerationOptions options) {
    // Extract constraints from product rules
    ProductConstraintExtractor.ProductConstraints constraints = ProductConstraintExtractor.extractConstraints(product);
    
    // Generate contract data using FeemiSavingsContractGenerator
    FeemiSavingsContractGenerator.GeneratedContractData contractData = 
        FeemiSavingsContractGenerator.generate(product, options);
    
    // Build the contract using the NewContract interface
    newContract
        .contractNumber(contractData.contract.contractNumber)
        .contractIssueDate(contractData.contract.issueDate)
        .contractStartDate(contractData.contract.startDate)
        .contractStatus("ACTIVE")
        .contractType("SAVINGS_INSURANCE")
        .contractSubType("FEEMI_SAVINGS")
        .contractData(contractData.contract.contractData)
        
        // Add policyholder as primary party
        .addParty(party -> {
          party
              .externalId(contractData.policyholder.personalId)
              .partyType("POLICYHOLDER")
              .partyEffectiveFrom(contractData.contract.startDate)
              .partyTermStartDate(contractData.contract.startDate)
              .partyData(contractData.policyholder.partyData);
        })
        
        // Add beneficiary if present
        .addParty(party -> {
          if (contractData.beneficiary != null) {
            party
                .externalId(contractData.beneficiary.personalId)
                .partyType("BENEFICIARY")
                .partyEffectiveFrom(contractData.contract.startDate)
                .partyTermStartDate(contractData.contract.startDate)
                .partyData(contractData.beneficiary.partyData);
          }
        })
        
        // Add coverage
        .addCoverage(coverage -> {
          coverage
              .insuredId(contractData.policyholder.personalId)
              .externalId(contractData.coverage.coverageCode)
              .coverageType(contractData.coverage.coverageType)
              .coverageCode(contractData.coverage.coverageCode)
              .coverageSumInsured(contractData.coverage.sumInsured)
              .coverageStatus("ACTIVE")
              .coverageEffectiveFrom(contractData.contract.startDate)
              .coverageTermStartDate(contractData.contract.startDate);
        })
        
        // Add payment plan
        .addPaymentPlan(paymentPlan -> {
          paymentPlan
              .paymentPlanStatus("ACTIVE")
              .paymentPlanFrequency(contractData.paymentPlan.frequency)
              .paymentPlanAmount(contractData.paymentPlan.monthlyAmount)
              .paymentPlanStartDate(contractData.paymentPlan.startDate);
        })
        
        // Add investment plan with allocations
        .addInvPlan(invPlan -> {
          invPlan
              .externalId("INV_PLAN_" + contractData.contract.contractNumber)
              .invPlanCode("FEEMI_SAVINGS_PLAN")
              .invPlanName("Feemi Savings Investment Plan")
              .invPlanStatus("ACTIVE")
              .invPlanStartDate(contractData.contract.startDate);
          
          // Add allocations
          for (FeemiSavingsContractGenerator.InvestmentAllocationData allocation : contractData.investmentAllocations) {
            invPlan.addAllocation(alloc -> {
              alloc
                  .invPlanAllocCode(allocation.fundCode)
                  .invPlanAllocName(allocation.fundName)
                  .invPlanAllocPercentage(allocation.percentage)
                  .invPlanAllocStatus("ACTIVE");
            });
          }
        })
        
        // Add product reference
        .addReference(reference -> {
          reference
              .referenceType("PRODUCT_CODE")
              .referenceValue(product.getProductCode());
        })
        
        // Add generation note
        .addNote(note -> {
          note
              .noteType("GENERATION_INFO")
              .noteValue("Generated using FeemiContractVisitor with realistic Finnish demographic data");
        });
  }
  
  public static void visitPensionContract(NewContract newContract, Product product, GenerationOptions options) {
    // Extract constraints from product rules
    ProductConstraintExtractor.ProductConstraints constraints = ProductConstraintExtractor.extractConstraints(product);
    
    // Generate person data
    CRM_Provider.Person person = CRM_Provider.generatePerson(
        options.getAgeRange().orElse(constraints.ageRange).getMinAge(),
        options.getIncomeRange().orElse(constraints.contributionRange).getMinIncome(),
        options.getIncomeRange().orElse(constraints.contributionRange).getMaxIncome()
    );
    
    // Generate fund data
    Fund_Provider.PaymentPlan paymentPlan = Fund_Provider.generatePaymentPlan(person.getEmployment().getAnnualIncome());
    Fund_Provider.Coverage coverage = Fund_Provider.generateCoverage("FEEMI_PENSION");
    
    // Build pension contract
    newContract
        .contractNumber("PEN-" + String.format("%08d", System.currentTimeMillis() % 100000000))
        .contractIssueDate(LocalDate.now().minusDays((long) (Math.random() * 365)))
        .contractStartDate(LocalDate.now().minusDays((long) (Math.random() * 30)))
        .contractStatus("ACTIVE")
        .contractType("PENSION_INSURANCE")
        .contractSubType("FEEMI_PENSION")
        
        // Add policyholder
        .addParty(party -> {
          party
              .externalId(person.getPersonalId())
              .partyType("POLICYHOLDER")
              .partyEffectiveFrom(LocalDate.now())
              .partyTermStartDate(LocalDate.now())
              .partyData(person.toJson());
        })
        
        // Add disability coverage
        .addCoverage(coverageBuilder -> {
          coverageBuilder
              .insuredId(person.getPersonalId())
              .externalId(coverage.getCoverageCode())
              .coverageType(coverage.getCoverageType())
              .coverageCode(coverage.getCoverageCode())
              .coverageSumInsured(coverage.getSumInsured())
              .coverageStatus("ACTIVE")
              .coverageEffectiveFrom(LocalDate.now())
              .coverageTermStartDate(LocalDate.now());
        })
        
        // Add payment plan
        .addPaymentPlan(paymentPlanBuilder -> {
          paymentPlanBuilder
              .paymentPlanStatus("ACTIVE")
              .paymentPlanFrequency(paymentPlan.getFrequency())
              .paymentPlanAmount(paymentPlan.getMonthlyAmount())
              .paymentPlanStartDate(paymentPlan.getStartDate());
        })
        
        // Add product reference
        .addReference(reference -> {
          reference
              .referenceType("PRODUCT_CODE")
              .referenceValue(product.getProductCode());
        });
  }
  
  public static void visitPSContract(NewContract newContract, Product product, GenerationOptions options) {
    // Extract constraints from product rules
    ProductConstraintExtractor.ProductConstraints constraints = ProductConstraintExtractor.extractConstraints(product);
    
    // Generate person data
    CRM_Provider.Person person = CRM_Provider.generatePerson(
        options.getAgeRange().orElse(constraints.ageRange).getMinAge(),
        options.getIncomeRange().orElse(constraints.contributionRange).getMinIncome(),
        options.getIncomeRange().orElse(constraints.contributionRange).getMaxIncome()
    );
    
    // Generate fund data
    Fund_Provider.PaymentPlan paymentPlan = Fund_Provider.generatePaymentPlan(person.getEmployment().getAnnualIncome());
    Fund_Provider.Coverage coverage = Fund_Provider.generateCoverage("FEEMI_PS");
    
    // Build PS contract (with government bonus)
    newContract
        .contractNumber("PS-" + String.format("%08d", System.currentTimeMillis() % 100000000))
        .contractIssueDate(LocalDate.now().minusDays((long) (Math.random() * 365)))
        .contractStartDate(LocalDate.now().minusDays((long) (Math.random() * 30)))
        .contractMaturityDate(LocalDate.now().plusYears(10)) // 10-year commitment
        .contractStatus("ACTIVE")
        .contractType("PS_INSURANCE")
        .contractSubType("FEEMI_PS")
        
        // Add policyholder
        .addParty(party -> {
          party
              .externalId(person.getPersonalId())
              .partyType("POLICYHOLDER")
              .partyEffectiveFrom(LocalDate.now())
              .partyTermStartDate(LocalDate.now())
              .partyData(person.toJson());
        })
        
        // Add government bonus coverage
        .addCoverage(coverageBuilder -> {
          coverageBuilder
              .insuredId(person.getPersonalId())
              .externalId(coverage.getCoverageCode())
              .coverageType(coverage.getCoverageType())
              .coverageCode(coverage.getCoverageCode())
              .coverageSumInsured(coverage.getSumInsured()) // 4.5% bonus
              .coverageStatus("ACTIVE")
              .coverageEffectiveFrom(LocalDate.now())
              .coverageTermStartDate(LocalDate.now())
              .coverageTermEndDate(LocalDate.now().plusYears(10));
        })
        
        // Add payment plan
        .addPaymentPlan(paymentPlanBuilder -> {
          paymentPlanBuilder
              .paymentPlanStatus("ACTIVE")
              .paymentPlanFrequency(paymentPlan.getFrequency())
              .paymentPlanAmount(paymentPlan.getMonthlyAmount())
              .paymentPlanStartDate(paymentPlan.getStartDate());
        })
        
        // Add product reference
        .addReference(reference -> {
          reference
              .referenceType("PRODUCT_CODE")
              .referenceValue(product.getProductCode());
        })
        
        // Add PS-specific note
        .addNote(note -> {
          note
              .noteType("GOVERNMENT_BONUS")
              .noteValue("Eligible for 4.5% annual government bonus with 10-year commitment");
        });
  }
}