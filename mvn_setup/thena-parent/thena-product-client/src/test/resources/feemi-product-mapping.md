# Feemi Product to Contract Entity Mapping

## Product Type Constants

```java
// Contract Types
public static final String SAVINGS_INSURANCE = "SAVINGS_INSURANCE";
public static final String PENSION_INSURANCE = "PENSION_INSURANCE"; 
public static final String PS_INSURANCE = "PS_INSURANCE";

// Product Codes (Contract Sub Types)
public static final String FEEMI_SAV_001 = "FEEMI_SAV_001";
public static final String FEEMI_PEN_001 = "FEEMI_PEN_001";
public static final String FEEMI_PS_001 = "FEEMI_PS_001";
```

## 1. Contract Entity Mapping

| Field | Feemi Savings | Feemi Pension | Feemi PS |
|-------|---------------|---------------|----------|
| `contractType` | "SAVINGS_INSURANCE" | "PENSION_INSURANCE" | "PS_INSURANCE" |
| `contractSubType` | "FEEMI_SAV_001" | "FEEMI_PEN_001" | "FEEMI_PS_001" |
| `contractStatus` | "ACTIVE", "SUSPENDED", "TERMINATED" | Same | Same |
| `contractIssueDate` | Date contract signed | Same | Same |
| `contractStartDate` | When coverage begins | Same | Same |
| `contractMaturityDate` | No fixed term (null) | Age 68 mandatory | 10+ years or age 68 |

### Contract Data JSON Structure

#### Savings Insurance (FEEMI_SAV_001)
```json
{
  "productCode": "FEEMI_SAV_001",
  "productName": "Säästö- ja sijoitusvakuutus",
  "eligibility": {
    "minAge": 18,
    "maxAge": 75,
    "maxAgeAtMaturity": 85,
    "residencyRequired": "FINNISH"
  },
  "contributions": {
    "initialPaymentMin": 1000,
    "initialPaymentMax": 50000,
    "regularPaymentMin": 50,
    "regularPaymentMax": 5000,
    "annualMax": 60000,
    "contractMinValue": 1000
  },
  "tax": {
    "contributionsDeductible": false,
    "capitalGainsRate": 0.30,
    "deathBenefitTaxFree": true,
    "earlyWithdrawalTaxAsIncome": true
  },
  "fees": {
    "setupFee": 0,
    "annualPolicyFee": 36,
    "surrenderFeeYear1": 0.02,
    "surrenderFeeYear2": 0.01,
    "surrenderFeeAfterYear2": 0,
    "withdrawalTransactionFee": 25
  }
}
```

#### Pension Insurance (FEEMI_PEN_001)
```json
{
  "productCode": "FEEMI_PEN_001", 
  "productName": "Vapaaehtoinen eläkevakuutus",
  "eligibility": {
    "minAge": 18,
    "maxAge": 62,
    "contributionStopAge": 68,
    "residencyRequired": "FINNISH"
  },
  "contributions": {
    "initialPaymentMin": 500,
    "initialPaymentMax": 8500,
    "regularPaymentMin": 25,
    "regularPaymentMax": 708,
    "annualTaxLimit": 8500,
    "employerLimit": 8500,
    "combinedMax": 17000,
    "annualMinimum": 300
  },
  "retirement": {
    "earliestAge": 62,
    "mandatoryAge": 68,
    "minContributionPeriod": 5,
    "earlyWithdrawalPenalty": 0.20
  },
  "tax": {
    "contributionsFullyDeductible": true,
    "paymentsAsIncome": true,
    "earlyWithdrawalPenalty": 0.20
  }
}
```

#### PS Insurance (FEEMI_PS_001)
```json
{
  "productCode": "FEEMI_PS_001",
  "productName": "PS-vakuutus (Pitkäaikaissäästäminen)",
  "eligibility": {
    "minAge": 18,
    "maxAge": 62,
    "conversionAge": 68,
    "residencyRequired": "FINNISH"
  },
  "contributions": {
    "initialPaymentMin": 50,
    "initialPaymentMax": 5000,
    "regularPaymentMin": 50,
    "regularPaymentMax": 416,
    "annualLimit": 5000,
    "lifetimeLimit": 50000,
    "annualMinimum": 50
  },
  "governmentBonus": {
    "rate": 0.045,
    "applicableUpTo": 5000,
    "paidQuarterly": true,
    "residencyRequired": true
  },
  "minimumHolding": {
    "years": 10,
    "earlyWithdrawalPenalty": 0.20,
    "bonusForfeitureOnEarly": true
  },
  "tax": {
    "contributionsNotDeductible": true,
    "governmentBonusTaxFree": true,
    "capitalGainsOnGrowthOnly": true
  }
}
```

## 2. Party Entity Mapping

| Field | Policyholder | Primary Beneficiary | Secondary Beneficiary |
|-------|--------------|-------------------|---------------------|
| `partyType` | "POLICYHOLDER" | "PRIMARY_BENEFICIARY" | "SECONDARY_BENEFICIARY" |
| `partyEffectiveFrom` | Contract start date | Same | Same |
| `partyData` | Full customer data | Basic beneficiary data | Basic beneficiary data |

### Party Data JSON Structures

#### Policyholder Data
```json
{
  "personalId": "010190-123A",
  "fullName": "Matti Virtanen",
  "dateOfBirth": "1990-01-01",
  "address": {
    "street": "Mannerheimintie 12",
    "postalCode": "00100",
    "city": "Helsinki",
    "country": "Finland"
  },
  "contact": {
    "phone": "+358401234567",
    "email": "matti.virtanen@example.fi"
  },
  "employment": {
    "status": "EMPLOYED",
    "annualIncome": 45000,
    "employer": "Tech Company Oy"
  },
  "banking": {
    "iban": "FI2112345600000785",
    "bankName": "Nordea"
  },
  "existingPensions": [
    {
      "type": "TyEL",
      "provider": "Varma",
      "monthlyAccrual": 150
    }
  ]
}
```

#### Beneficiary Data  
```json
{
  "personalId": "120285-234B",
  "fullName": "Anna Virtanen", 
  "relationship": "SPOUSE",
  "percentage": 100,
  "address": {
    "street": "Mannerheimintie 12",
    "postalCode": "00100", 
    "city": "Helsinki",
    "country": "Finland"
  }
}
```

## 3. PaymentPlan Entity Mapping

| Field | Savings | Pension | PS |
|-------|---------|---------|-----|
| `paymentPlanFrequency` | "MONTHLY", "QUARTERLY", "ANNUAL" | Same | Same |
| `paymentPlanAmount` | 50-5000 (monthly) | 25-708 (monthly) | 50-416 (monthly) |
| `paymentPlanStatus` | "ACTIVE", "SUSPENDED" | Same | Same |
| `paymentPlanStartDate` | Contract start | Same | Same |
| `paymentPlanEndDate` | Open-ended | Age 68 | Age 68 |

## 4. InvPlan Entity Mapping

### Investment Plan Codes and Names

#### Savings Insurance Options
- `TAATTU_TUOTTO` → "Taattu tuotto" (0.5% guaranteed)
- `TASAPAINOINEN_RAHASTO` → "Tasapainoinen rahasto" (60/40 bonds/equity)
- `OSAKERAHASTO` → "Osakerahasto" (90% equity)
- `INDEKSIRAHASTO` → "Indeksirahasto" (MSCI World)

#### Pension Insurance Options  
- `TAATTU_ELAKETUOTTO` → "Taattu eläketuotto" (1.0% guaranteed)
- `MALTILLINEN_ELAKESALKKU` → "Maltillinen eläkesalkku" (80/20 bonds/equity)
- `TASAPAINOINEN_ELAKESALKKU` → "Tasapainoinen eläkesalkku" (50/50)
- `KASVU_ELAKESALKKU` → "Kasvueläkesalkku" (20/80 bonds/equity)
- `LIFECYCLE` → "Elinkaarisalkku" (age-based allocation)

#### PS Insurance Options
- `TAATTU_PS_TUOTTO` → "Taattu PS-tuotto" (1.5% guaranteed)
- `MALTILLINEN_PS_RAHASTO` → "Maltillinen PS-rahasto" (70/30 bonds/equity)  
- `TASAPAINOINEN_PS_RAHASTO` → "Tasapainoinen PS-rahasto" (50/50)
- `KASVU_PS_RAHASTO` → "Kasvu PS-rahasto" (20/80 bonds/equity)

## 5. InvPlanAlloc Entity Mapping

| Field | Value | Rules |
|-------|-------|-------|
| `invPlanAllocCode` | Fund code from above | Must be valid for product type |
| `invPlanAllocName` | Fund name from above | Same |
| `invPlanAllocPercentage` | 0.10 - 1.00 | Must total 100% across all allocations |
| `invPlanAllocStatus` | "ACTIVE" | Standard status |

### Allocation Rules by Product
- **Savings**: Minimum 10% per fund, max 100% in single fund
- **Pension**: Same, plus lifecycle option
- **PS**: Minimum 25% per fund if multiple selected

## 6. Coverage Entity Mapping

### Death Benefits
| Product | Coverage Type | Coverage Code | Sum Insured |
|---------|---------------|---------------|-------------|
| Savings | "DEATH_BENEFIT" | "DEATH_1000" | EUR 1,000 + account value |
| Pension | "DEATH_BENEFIT" | "DEATH_ACCOUNT" | Account value |
| PS | "DEATH_BENEFIT" | "DEATH_ACCOUNT_BONUS" | Account + government bonus |

### Special Coverages  
| Product | Coverage Type | Coverage Code | Description |
|---------|---------------|---------------|-------------|
| PS | "GOVERNMENT_BONUS" | "PS_BONUS_4_5" | 4.5% annual government bonus |
| Pension | "TAX_BENEFIT" | "TAX_DEDUCTION" | Tax deductibility |

## 7. Business Rule Validation

### Age Validation
```java
// Savings: 18-75 at inception, max 85 at maturity
// Pension: 18-62 at inception, stop contributions at 68
// PS: 18-62 at inception, convert at 68
```

### Contribution Validation  
```java
// Annual limits: Savings=60k, Pension=8.5k, PS=5k
// Minimum sustaining: Savings=1k, Pension=300, PS=50
```

### Investment Allocation Validation
```java
// Must total 100%, minimum percentages vary by product
```

This mapping provides the foundation for realistic test data generation that respects all business rules and constraints from the Feemi product specifications.