# Yearly Calculation Formulas for FEEMI_SAV_001

## 1. Annual Policy Fee Distribution

**Formula:**
Policy Fee per Fund Subscription = (Fund Subscription Value / Total Portfolio Value) × EUR 36

**Description:** Distributes the annual EUR 36 policy fee proportionally across all fund subscriptions based on their portfolio weight.

**Example:**
- Portfolio: EUR 20,000 total (EUR 12,000 Balanced Fund + EUR 8,000 Equity Fund)
- Balanced Fund fee = (12,000 / 20,000) × 36 = EUR 21.60
- Equity Fund fee = (8,000 / 20,000) × 36 = EUR 14.40

## 2. Yearly Performance Calculation

**Formula:**
Yearly Performance per Fund Subscription = Current Subscription Value - Starting Subscription Value

**Description:** Calculates the absolute change in fund subscription value over the year, including growth from monthly calculations and new payments.

**Example:**
- Balanced Fund: Started at EUR 10,000, now EUR 12,000
- Yearly Performance = 12,000 - 10,000 = EUR 2,000

## 3. Fund Performance Tracking

**Formula:**
Fund Performance % = (End Price - Start Price) / Start Price × 100

**Description:** Tracks the underlying fund's unit price performance to compare against subscription performance.

**Example:**
- Fund unit price: Started at EUR 10.50, now EUR 11.30
- Fund Performance = (11.30 - 10.50) / 10.50 × 100 = 7.62%

## 4. Net Yearly Change

**Formula:**
Net Yearly Change per Fund Subscription = Yearly Performance per Fund Subscription - Policy Fee per Fund Subscription

**Description:** The actual contribution each fund subscription makes to portfolio value after deducting its proportional share of the annual policy fee.

**Example:**
- Balanced Fund performance: EUR 2,000
- Balanced Fund policy fee: EUR 21.60
- Net Yearly Change = 2,000 - 21.60 = EUR 1,978.40

## 5. Portfolio Totals

**Formula:**
Total Policy Fees = sum(Policy Fee per Fund Subscription)
Total Performance = sum(Yearly Performance per Fund Subscription)Total Net Change = sum(Net Yearly Change per Fund Subscription)

**Description:** Aggregates all fund subscription values to get portfolio-wide totals for the year.

**Example:**
- Total Policy Fees = 21.60 + 14.40 = EUR 36.00
- Total Performance = 2,000 + 800 = EUR 2,800
- Total Net Change = 1,978.40 + 785.60 = EUR 2,764

## 6. New Portfolio Balance

**Formula:**
New Portfolio Balance = Previous Balance + Total Net Change

**Description:** Updates the portfolio balance after applying yearly performance gains and deducting annual policy fees.

**Example:**
- Previous Balance: EUR 20,000
- Total Net Change: EUR 2,764
- New Portfolio Balance = 20,000 + 2,764 = EUR 22,764