# Monthly Calculation Formulas for FEEMI_SAV_001

## 1. Monthly Growth Rate Calculation

**Formula:**
Monthly Growth Amount per Fund Subscription = Current Subscription Value × (μ ÷ 12)

Where μ = 0.05 (5% annual return rate)
Monthly Rate = 0.05 ÷ 12 = 0.004166... (≈0.417%)

**Description:** Calculates the estimated monthly growth for each fund subscription based on the annual expected return rate of 5%, applied proportionally per month.

**Example:**
- Current Balanced Fund Value: EUR 12,000
- Monthly Growth = 12,000 × 0.004166 = EUR 50.00

## 2. Monthly Mortality Fee Calculation

**Formula:**
Monthly Mortality Fee per Fund Subscription = Current Subscription Value × (γ ÷ 12)

Where γ = 0.001 (0.1% annual mortality rate)
Monthly Rate = 0.001 ÷ 12 = 0.000083... (≈0.0083%)

**Description:** Applies the monthly portion of the annual mortality fee (insurance cost) proportionally to each fund subscription value.

**Example:**
- Current Balanced Fund Value: EUR 12,000
- Monthly Mortality Fee = 12,000 × 0.000083 = EUR 1.00

## 3. Net Monthly Growth per Fund Subscription

**Formula:**
Net Monthly Growth per Fund Subscription = Monthly Growth Amount - Monthly Mortality Fee

**Description:** The actual growth contribution after deducting mortality fees from the estimated investment growth.

**Example:**
- Monthly Growth: EUR 50.00
- Monthly Mortality Fee: EUR 1.00
- Net Monthly Growth = 50.00 - 1.00 = EUR 49.00

## 4. New Fund Subscription Value

**Formula:**
New Fund Subscription Value = Current Subscription Value + Net Monthly Growth

**Description:** Updates each fund subscription value by adding the net monthly growth after all fees.

**Example:**
- Current Balanced Fund Value: EUR 12,000
- Net Monthly Growth: EUR 49.00
- New Balanced Fund Value = 12,000 + 49.00 = EUR 12,049.00

## 5. Portfolio Aggregation

**Formula:**
Total Gross Growth = sum(Monthly Growth Amount per Fund Subscription)
Total Mortality Fees = sum(Monthly Mortality Fee per Fund Subscription)
Total Net Growth = sum(Net Monthly Growth per Fund Subscription)

**Description:** Aggregates all fund subscription calculations to get portfolio-wide monthly totals.

**Example:**
- Balanced Fund Growth: EUR 50.00, Mortality: EUR 1.00, Net: EUR 49.00
- Equity Fund Growth: EUR 33.33, Mortality: EUR 0.67, Net: EUR 32.66
- Total Gross Growth = 50.00 + 33.33 = EUR 83.33
- Total Mortality Fees = 1.00 + 0.67 = EUR 1.67
- Total Net Growth = 49.00 + 32.66 = EUR 81.66

## 6. New Portfolio Balance

**Formula:**
New Portfolio Balance = Previous Portfolio Balance + Total Net Growth

**Description:** Updates the total portfolio balance by adding the aggregated net monthly growth across all fund subscriptions.

**Example:**
- Previous Portfolio Balance: EUR 20,000
- Total Net Growth: EUR 81.66
- New Portfolio Balance = 20,000 + 81.66 = EUR 20,081.66

## 7. Calculation Trigger Rules

**Condition:**
Monthly calculation executes only if >= 30 days have passed since the last monthly calculation.

**Description:** Prevents duplicate monthly calculations and ensures proper timing intervals.

**Example:**
- Last Calculation: 2024-11-15
- Current Date: 2024-12-16
- Days Elapsed: 31 days >= 30 days → Execute monthly calculation

## 8. Fund Price Integration

**Formula:**
Current Fund Unit Price = Fund Query Result for Calculation Date

**Description:** Retrieves the current unit price for each fund on the calculation date for reference and potential unit calculations.

**Example:**
- Fund Code: "BALANCED_FUND_001"
- Calculation Date: 2024-12-15
- Retrieved Unit Price: EUR 11.30

## Product Parameters for FEEMI_SAV_001

- **μ (Mu)**: 0.05 (5% annual return rate)
- **γ (Gamma)**: 0.001 (0.1% annual mortality rate)
- **Monthly Return Rate**: μ ÷ 12 = 0.004166...
- **Monthly Mortality Rate**: γ ÷ 12 = 0.000083...
- **Calculation Frequency**: Monthly (minimum 30-day interval)