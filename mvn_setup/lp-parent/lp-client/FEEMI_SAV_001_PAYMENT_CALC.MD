# Payment Calculation Formulas for FEEMI_SAV_001

## 1. Payment Fee Calculation

**Formula:**
Payment Fee = Gross Payment Amount × κ

Where κ = 0.00 (0% payment fee rate for FEEMI_SAV_001)

**Description:** Calculates any fees deducted from the gross payment amount. For FEEMI_SAV_001, there are no payment fees, so this calculation results in zero.

**Example:**
- Gross Payment: EUR 1,000
- Payment Fee = 1,000 × 0.00 = EUR 0.00

## 2. Net Payment Amount

**Formula:**
Net Payment Amount = Gross Payment Amount - Payment Fee

**Description:** The amount available for allocation to investment plans after deducting payment fees.

**Example:**
- Gross Payment: EUR 1,000
- Payment Fee: EUR 0.00
- Net Payment = 1,000 - 0.00 = EUR 1,000.00

## 3. Fund Allocation by Percentage

**Formula:**
Allocated Amount per Fund = Net Payment Amount × Allocation Percentage

**Description:** Distributes the net payment across different fund allocations according to predefined percentage splits in the investment plan.

**Example:**
- Net Payment: EUR 1,000
- Balanced Fund Allocation: 60%
- Equity Fund Allocation: 40%
- Balanced Fund Amount = 1,000 × 0.60 = EUR 600.00
- Equity Fund Amount = 1,000 × 0.40 = EUR 400.00

## 4. Fund Unit Purchase Calculation

**Formula:**
Fund Units Purchased = Allocated Amount ÷ Fund Unit Price

**Description:** Converts the allocated money amount into fund units based on the current unit price on the payment date.

**Example:**
- Balanced Fund Allocation: EUR 600.00
- Balanced Fund Unit Price: EUR 10.50
- Fund Units = 600.00 ÷ 10.50 = 57.142857 units

## 5. Mortality Fee per Allocation

**Formula:**
Mortality Fee per Allocation = Allocated Amount × γ

Where γ = 0.001 (0.1% mortality rate for death cover)

**Description:** Applies the mortality fee (insurance cost) to each fund allocation amount.

**Example:**
- Balanced Fund Allocation: EUR 600.00
- Mortality Fee = 600.00 × 0.001 = EUR 0.60

## 6. Net Allocation Amount

**Formula:**
Net Allocation Amount = Allocated Amount - Mortality Fee

**Description:** The final amount that contributes to the fund subscription value after deducting mortality fees.

**Example:**
- Allocated Amount: EUR 600.00
- Mortality Fee: EUR 0.60
- Net Allocation = 600.00 - 0.60 = EUR 599.40

## 7. Portfolio Total Calculations

**Formula:**
Total Allocated = sum(Allocated Amount per Fund)
Total Inflow = sum(Gross Payment Amount per Fund)
Total Outflow = sum(Payment Fee + Mortality Fee per Fund)
Total Net Change = Total Inflow - Total Outflow

**Description:** Aggregates all fund allocation calculations to get portfolio-wide payment processing totals.

**Example:**
- Balanced Fund: Allocated EUR 600.00, Inflow EUR 600.00, Outflow EUR 0.60
- Equity Fund: Allocated EUR 400.00, Inflow EUR 400.00, Outflow EUR 0.40
- Total Allocated = 600.00 + 400.00 = EUR 1,000.00
- Total Inflow = 600.00 + 400.00 = EUR 1,000.00
- Total Outflow = 0.60 + 0.40 = EUR 1.00
- Total Net Change = 1,000.00 - 1.00 = EUR 999.00

## 8. New Portfolio Balance

**Formula:**
New Portfolio Balance = Previous Portfolio Balance + Total Net Change

**Description:** Updates the total portfolio balance by adding the net change from the processed payment.

**Example:**
- Previous Portfolio Balance: EUR 20,000.00
- Total Net Change: EUR 999.00
- New Portfolio Balance = 20,000.00 + 999.00 = EUR 20,999.00

## 9. Payment Processing Rules

**Conditions:**
- Only processes MoneyRequest entries with status = OPEN
- Only processes MoneyRequest entries with type = ADD_PAYMENT
- Links payments via externalId or paymentId matching
- Marks processed MoneyRequest entries as CLOSED

**Description:** Ensures payments are processed exactly once and properly tracked through the system.

## 10. Fund Price Lookup

**Formula:**
Fund Unit Price = Fund Query Result for Payment Date

**Description:** Retrieves the unit price for each fund on the specific payment date to ensure accurate unit calculations.

**Example:**
- Fund Code: "BALANCED_FUND_001"
- Payment Date: 2024-12-15
- Retrieved Unit Price: EUR 10.50

## Product Parameters for FEEMI_SAV_001

- **κ (Kappa)**: 0.00 (0% payment fee rate)
- **γ (Gamma)**: 0.001 (0.1% mortality rate for death cover)
- **μ (Mu)**: 0.05 (5% estimated return rate - reference only)
- **Processing**: Real-time when payment requests are submitted
- **Fund Unit Precision**: 6 decimal places
- **Money Amounts Precision**: 2 decimal places (rounded to nearest cent)