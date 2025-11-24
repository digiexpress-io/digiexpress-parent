# Financial Calculation Patterns Guide

This document outlines the established patterns for implementing financial calculations in the LP client system.

## Overview

The system uses an **Abstract Syntax Tree (AST) visitor pattern** for complex financial calculations with hierarchical data processing and comprehensive audit trails.

## Core Principles

### 1. AST Hierarchy Pattern
All calculations follow a three-level hierarchical structure:
```
TopLevel (e.g., MoneyToLedger, MonthlyPortfolioGrowth)
├── MiddleLevel (e.g., PaymentToInvPlan, MonthlyInvPlanGrowth)  
    └── LeafLevel (e.g., PaymentToInvPlanAlloc, MonthlyInvPlanAllocGrowth)
```

### 2. Expression-Node Pattern
Each level has two components:
- **Expression**: Input data structure (context/parameters)
- **Node**: Output data structure (calculated results)

### 3. Visitor Pattern Flow
- **Top-down traversal**: Start with high-level request, drill down through hierarchy
- **Bottom-up aggregation**: Calculate values at leaf level, sum upwards
- **Side effects**: Create BlackBook entries with detailed money tracking

## Implementation Structure

### Directory Layout
```
formula/
└── {product_name}/           # e.g., feemi_savings
    ├── BlackBookConstants.java
    ├── addpayment/
    │   ├── AddPayment_{Product}.java
    │   └── ast/
    │       ├── MoneyToLedger.java
    │       ├── PaymentToInvPlan.java
    │       └── PaymentToInvPlanAlloc.java
    └── monthly/
        ├── Monthly_{Product}.java
        └── ast/
            ├── MonthlyPortfolioGrowth.java
            ├── MonthlyInvPlanGrowth.java
            └── MonthlyInvPlanAllocGrowth.java
```

### Factory Pattern
```java
@RequiredArgsConstructor
public class AddPaymentFactory implements CalculationFormula {
  @Override
  public Uni<Envelope<AnyCalculation>> accept(FormulaContainer container) {
    final var resolver = container.getContract().getContract().getContractSubType().orElse("");
    switch (resolver) {
      case "FEEMI_SAVINGS": return new AddPayment_FeemiSavings().accept(container);
      default: throw new IllegalArgumentException("Unexpected value: " + resolver);
    }
  }  
}
```

## Calculation Implementation Rules

### 1. Main Formula Class Structure
```java
@RequiredArgsConstructor
public class {CalculationType}_{Product} implements CalculationFormula {
  
  // Context and state
  private FormulaContainer ctx;
  private MergeLedger ledger;
  private NewBlackBook newBlackBook;
  private BlackBook lastBlackBook;
  
  // Product parameters as constants
  private static final BigDecimal PARAM_NAME = new BigDecimal("value");
  
  public Uni<Envelope<AnyCalculation>> accept(FormulaContainer container) {
    this.ctx = container;
    
    // Implementation logic
    
    return ctx.getLedgerClient().withTenant().commit().modifyOneLedger()
      .commitMessage("+ calculation description")
      .commitAuthor(ClassName.class.getSimpleName())
      .ledgerId(ctx.getLedger().getLedger().getId())
      .modifyLedger(ledger -> {
        this.ledger = ledger;
        
        // Execute main calculation
        visitTopLevelCalculation();
        
        this.ledger.build();
      }).build()
      .onItem().transform(env -> /* map to result */);
  }
}
```

### 2. AST Node Classes
```java
public class NodeName {
  
  @Value
  public static class Expression {
    // Input parameters
    Payment payment;
    InvPlan invPlan;
    InvPlanAlloc allocation;
  }
  
  @Data @Builder @Jacksonized
  public static class Node {
    private final List<String> logs;           // Mathematical formula logs
    private final String targetId;             // Reference ID
    private final BigDecimal calculatedValue;  // Main result
    // ... other calculated fields
  }
}
```

### 3. Visitor Method Pattern
```java
private NodeType.Node visitNodeCalculation(NodeType.Expression exp) {
  // Extract parameters
  final var param1 = exp.getParam1();
  final var param2 = exp.getParam2();
  
  // Perform calculations
  final var result = calculateValue(param1, param2);
  
  // Create audit logs
  final var logs = new ArrayList<String>();
  logs.add(String.format("Formula: param1 × param2 = %s × %s = %s", param1, param2, result));
  
  // For non-leaf nodes: iterate through children and aggregate
  if (hasChildren) {
    for (final var child : getChildren()) {
      final var childNode = visitChildCalculation(new ChildType.Expression(child));
      // Aggregate child results
    }
  }
  
  // Create BlackBook entries (if applicable)
  if (shouldCreateEntry) {
    newBlackBook.addBlackBookDetail(bbd -> bbd
        .type(BlackBookConstants.DETAIL_TYPE_...)
        .subType(BlackBookConstants.SUBTYPE_...)
        .amount(result)
        .targetId(targetId)
        .body(JsonObject.mapFrom(node))
        .build());
  }
  
  return NodeType.Node.builder()
      .logs(logs)
      .calculatedValue(result)
      .build();
}
```

## Money Flow Tracking

### 4. BlackBook Entry Rules
Always track money flows with:
- **amount**: Running total or final calculated value
- **deltaAmount**: Net change (positive or negative)  
- **inflowAmount**: Money coming in (always positive)
- **outflowAmount**: Money going out (always positive)

### 5. Use Constants
Always use `BlackBookConstants` for type classifications:
```java
// BlackBook Types
BlackBookConstants.TYPE_INCOMING_PAYMENT
BlackBookConstants.TYPE_MONTHLY_CALCULATION

// Detail Types  
BlackBookConstants.DETAIL_TYPE_PAYMENT_DETAIL
BlackBookConstants.DETAIL_TYPE_MONTHLY_DETAIL

// Detail SubTypes
BlackBookConstants.SUBTYPE_PAYMENT_ALLOCATED_AMOUNT
BlackBookConstants.SUBTYPE_ALLOCATION_GROWTH
```

## Ledger Tree Navigation

### 6. Use Tree API for Temporal Queries
Instead of manual date filtering:
```java
// Get tree navigation
final var tree = ctx.getLedger().toTree();

// Navigate back to specific calculation type
final var nodesSinceLastMonthly = tree.getTill(BlackBookConstants.TYPE_MONTHLY_CALCULATION);

// Find baseline values and calculate deltas
final var baselineValue = findLastCalculationValue(nodesSinceLastMonthly);
final var newAllocations = sumNewAllocationsSince(nodesSinceLastMonthly);
```

## Fund Integration

### 7. Fund Price Lookup
```java
// Get fund unit price for calculations
final var fund = ctx.getFundQuery().getOne(fundCode, calculationDate);
final var unitPrice = fund.getCalculationValue().getPriceValue();

// Calculate fund units
final var fundUnits = allocatedAmount.divide(unitPrice, 6, RoundingMode.HALF_UP);
```

## Product Parameters

### 8. Insurance Product Constants
Define product-specific parameters as constants:
```java
// FEEMI_SAV_001 product parameters
private static final BigDecimal KAPPA = BigDecimal.ZERO;           // Payment fee rate
private static final BigDecimal GAMMA = new BigDecimal("0.001");   // Mortality rate  
private static final BigDecimal MU = new BigDecimal("0.05");       // Return rate
```

## Audit Trail Requirements

### 9. Mathematical Logging
Every calculation must log its mathematical formula:
```java
logs.add(String.format("Payment Fee: κ × gross_amount = %s × %s = %s", KAPPA, grossAmount, feeAmount));
logs.add(String.format("Net Payment: gross_amount - payment_fee = %s - %s = %s", grossAmount, feeAmount, netAmount));
```

### 10. JSON Serialization
Store complete calculation context:
```java
.body(JsonObject.mapFrom(calculationNode))  // Full node with all calculations
```

## Error Handling

### 11. Envelope Pattern
Always return results wrapped in Envelope:
```java
return ImmutableEnvelope.<AnyCalculation>builder()
    .status(success ? EnvelopeStatus.OK : EnvelopeStatus.ERROR)
    .addLogs(auditLogs)
    .object(calculationResult)
    .build();
```

## Testing Considerations

### 12. Calculation Verification
- Verify mathematical formulas match product specifications
- Test edge cases (zero amounts, missing data)
- Validate BlackBook entry creation
- Check audit trail completeness
- Ensure proper money flow tracking

## Example: Adding New Product

1. Create directory: `formula/{new_product}/`
2. Add `BlackBookConstants.java` with product-specific constants
3. Implement calculation types (addpayment, monthly, etc.)
4. Create AST hierarchy for each calculation type
5. Update factory classes to route new product
6. Add comprehensive tests

This pattern ensures consistency, auditability, and maintainability across all financial calculations.