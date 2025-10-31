# Nova Virtus Product to Contract Entity Mapping

## Product Type Constants

```java
// Contract Types
public static final String ENDOWMENT_INSURANCE = "ENDOWMENT_INSURANCE";

// Product Codes (Contract Sub Types)
public static final String NOVA_VIR_001 = "NOVA_VIR_001";
```

## 1. Contract Entity Mapping

| Field | Nova Virtus |
|-------|-------------|
| `contractType` | "ENDOWMENT_INSURANCE" |
| `contractSubType` | "NOVA_VIR_001" |
| `contractStatus` | "ACTIVE", "SUSPENDED", "TERMINATED" |
| `contractIssueDate` | Date contract signed |
| `contractStartDate` | When coverage begins |
| `contractMaturityDate` | No fixed term (null) - flexible endowment |

### Contract Data JSON Structure

#### Nova Virtus Endowment (NOVA_VIR_001)
```json
{
  "productCode": "NOVA_VIR_001",
  "productName": "Nova Virtus Säästöhenkivakuutus",
  "issuer": {
    "name": "Nova Life Assurance Finland Ltd",
    "businessId": "0927072-8"
  },
  "eligibility": {
    "minAge": null,
    "maxAge": null,
    "residencyRequired": "FINNISH_OR_EU",
    "minInvestment": 10
  },
  "fees": {
    "managementFee": {
      "standardRate": 0.004,
      "standardThreshold": 100000,
      "aboveThresholdRate": 0.001,
      "under30Rate": 0.002,
      "under30Threshold": 100000,
      "maximumRate": 0.01
    },
    "transactionFees": {
      "etfTradingFee": 0.0015,
      "pledgingConfirmation": 50,
      "extraAccountStatement": 10
    },
    "inheritanceCover": {
      "chargedWhenNeeded": true,
      "freeIfGrowth": true
    },
    "indexationAllowed": true
  },
  "investmentOptions": {
    "investmentBaskets": {
      "count": "40+",
      "types": ["GRANITE_PORTFOLIOS", "GLOBE_BASKETS"]
    },
    "investmentFunds": {
      "count": "400+",
      "riskVariety": true
    },
    "etfs": {
      "count": 35,
      "monthlySaving": true,
      "noMinimum": true
    },
    "equityBaskets": {
      "count": 14,
      "exchange": "NASDAQ_OMX_HELSINKI"
    },
    "insuranceAccount": {
      "lowRisk": true,
      "yield": "1W_EURIBOR_MINUS_0_4"
    }
  },
  "taxation": {
    "switchingTaxFree": true,
    "withdrawalTaxed": true,
    "automaticWithholding": true,
    "lossesDeductible": true,
    "inheritanceTaxRules": {
      "nextOfKin": "INHERITANCE_TAX",
      "others": "CAPITAL_INCOME_TAX"
    }
  },
  "inheritancePlanning": {
    "beneficiaryClause": true,
    "flexibleAmendment": true,
    "maritalRightsExclusion": true,
    "inheritanceCover": {
      "protectsCapital": true,
      "marketFluctuationProtection": true,
      "conditionalCharging": true
    }
  }
}
```

## 2. Party Entity Mapping

| Field | Policyholder | Primary Beneficiary | Secondary Beneficiary |
|-------|--------------|-------------------|---------------------|
| `partyType` | "POLICYHOLDER" | "PRIMARY_BENEFICIARY" | "SECONDARY_BENEFICIARY" |
| `partyEffectiveFrom` | Contract start date | Same | Same |
| `partyData` | Full customer data | Beneficiary data | Beneficiary data |

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
    "bankName": "Nova Bank"
  },
  "ageCategory": "UNDER_30",
  "investmentExperience": "MODERATE",
  "riskTolerance": "MEDIUM"
}
```

#### Beneficiary Data  
```json
{
  "personalId": "120285-234B",
  "fullName": "Anna Virtanen", 
  "relationship": "SPOUSE",
  "relationshipType": "NEXT_OF_KIN",
  "percentage": 100,
  "maritalRightsExcluded": false,
  "address": {
    "street": "Mannerheimintie 12",
    "postalCode": "00100", 
    "city": "Helsinki",
    "country": "Finland"
  }
}
```

## 3. PaymentPlan Entity Mapping

| Field | Nova Virtus |
|-------|-------------|
| `paymentPlanFrequency` | "FLEXIBLE" (no fixed contributions) |
| `paymentPlanAmount` | EUR 10+ (minimum investment) |
| `paymentPlanStatus` | "ACTIVE", "SUSPENDED" |
| `paymentPlanStartDate` | Contract start |
| `paymentPlanEndDate` | Open-ended (null) |

**Note**: Nova Virtus is flexible - no regular payment plan required, just minimum EUR 10 per investment.

## 4. InvPlan Entity Mapping

### Investment Plan Codes and Names

#### Investment Baskets (40+ options)
- `GRANITE_PORTFOLIO_1` → "Granite Portfolio Conservative"
- `GRANITE_PORTFOLIO_2` → "Granite Portfolio Balanced" 
- `GRANITE_PORTFOLIO_3` → "Granite Portfolio Growth"
- `GLOBE_BASKET_ESG` → "Globe Basket ESG"
- `GLOBE_BASKET_EMERGING` → "Globe Basket Emerging Markets"

#### Investment Funds (400+ options)
- `NOVA_FUND_CONSERVATIVE` → "Nova Conservative Fund"
- `NOVA_FUND_BALANCED` → "Nova Balanced Fund"
- `NOVA_FUND_GROWTH` → "Nova Growth Fund"
- `NOVA_FUND_GLOBAL_EQUITY` → "Nova Global Equity Fund"

#### ETFs (35 options)
- `NOVA_ETF_WORLD` → "Nova World ETF"
- `NOVA_ETF_EUROPE` → "Nova Europe ETF"
- `NOVA_ETF_EMERGING` → "Nova Emerging Markets ETF"
- `NOVA_ETF_BONDS` → "Nova Bond ETF"

#### Equity Baskets (14 options)
- `EQUITY_NOKIA` → "Nokia Equity Basket"
- `EQUITY_FORTUM` → "Fortum Equity Basket"
- `EQUITY_UPM` → "UPM Equity Basket"
- `EQUITY_STORA_ENSO` → "Stora Enso Equity Basket"

#### Special Options
- `INSURANCE_ACCOUNT` → "Nova Insurance Account" (1W Euribor - 0.4%)

## 5. InvPlanAlloc Entity Mapping

| Field | Value | Rules |
|-------|-------|-------|
| `invPlanAllocCode` | Fund/basket code from above | Must be valid Nova option |
| `invPlanAllocName` | Fund/basket name from above | Same |
| `invPlanAllocPercentage` | 0.01 - 1.00 | Must total 100% across all allocations |
| `invPlanAllocStatus` | "ACTIVE" | Standard status |

### Allocation Rules
- **Flexibility**: No minimum percentage restrictions mentioned
- **Switching**: Allowed without tax implications
- **ETF Trading**: 0.15% fee per transaction
- **Monthly Saving**: Available for ETFs with no minimums

## 6. Coverage Entity Mapping

### Inheritance Cover
| Coverage Type | Coverage Code | Description |
|---------------|---------------|-------------|
| "INHERITANCE_COVER" | "CAPITAL_PROTECTION" | Guarantees invested capital to beneficiaries |
| "INHERITANCE_COVER" | "MARKET_PROTECTION" | Protection against market fluctuations |

### Standard Death Benefit
| Coverage Type | Coverage Code | Sum Insured |
|---------------|---------------|-------------|
| "DEATH_BENEFIT" | "ACCOUNT_VALUE" | Current account value |
| "DEATH_BENEFIT" | "INHERITANCE_PLANNING" | Enhanced for estate planning |

## 7. Business Rule Validation

### Investment Validation
```java
// Minimum investment: EUR 10
// No maximum limits specified
// Age-based fee calculation (under 30 vs standard)
```

### Fee Calculation Rules
```java
// Management fees:
// - Under 30: 0.2% up to EUR 100k, 0.1% above
// - Standard: 0.4% up to EUR 100k, 0.1% above
// - Maximum allowed: 1.0%

// ETF trading: 0.15% per transaction
// Other fees: Fixed EUR amounts
```

### Tax Validation
```java
// Investment switching: No immediate tax
// Withdrawals: Proportional capital/return calculation
// Inheritance: Different rules for next-of-kin vs others
```

### Inheritance Planning Rules
```java
// Beneficiary clause: Flexible, amendable
// Marital rights: Can be excluded
// Estate planning: Direct benefit payment
// Cover charging: Only when savings < capital
```

## 8. Special Features Integration

### Nova Advantage Program
- Banking customer benefits
- Fee discounts based on relationship
- Enhanced digital services

### Digital Integration
- Online investment tracking
- Automatic rebalancing options
- Market volatility protection

### Regulatory Compliance
- Not covered by deposit guarantee
- Not covered by Investors' Compensation Fund
- Risk warnings required
- Tax situation disclaimers

This mapping provides the foundation for generating realistic Nova Virtus test data that respects the authentic fee structures, investment options, and inheritance planning features from the real Nordea product specifications.