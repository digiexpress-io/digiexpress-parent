# Financial Ledger System Architecture

## System Overview

**Purpose:** Process contract commands into financial intelligence for institutional risk trading and partner organization operations.

**Flow:** `COBOL → Orchestration → Contract Commands → LEDGER → Sys Log → Human Analysis → Partner Deals`

## Core Architecture

### Data Sources
- **COBOL Core Banking Systems:** Source of all financial transactions (untouchable, proven infrastructure)
- **Orchestration Layer:** Matches COBOL events to contracts, generates structured commands
- **Contract System:** Stores commands and contract configuration data

### Ledger Processing Engine
- **Input:** Contract commands (premium payments, coverage changes, corrections, life events)
- **Processing:** Apply tenant-specific business rules, calculate financial state, generate audit trails
- **Output:** Immutable ledger entries + sys log events for external consumption

### Data Consumers
- **Sys Log Queries:** Daily/weekly/monthly orchestrators feeding external systems
- **Accounting Systems:** Portfolio aggregation for human analysis
- **Investment Product Development:** Risk repackaging and institutional sales
- **Financial Partner Operations:** Phone-based deal making and profit settlement

## Scale & Tenant Model

### Per-Tenant Characteristics
- **Size:** 100K-200K policies per tenant
- **Volume:** ~960M entries/year per tenant (including life events, corrections, calculations)
- **Lifespan:** Policies active 30-40+ years (last expiry 2055)
- **Business Logic:** Each tenant = separate legacy business with distinct calculation rules

### Data Volume Reality
- **Signal vs Noise:** 30% business data, 70% audit/compliance trails (like FT8 protocol)
- **Math Logs:** Detailed calculation breakdowns for regulatory compliance and profit optimization
- **Correction Entries:** Immutable-only corrections (no database mutations)
- **Total Scale:** Tens of billions of entries per tenant over policy lifetime

## Business Context

### EU Regulatory Environment
- Business stakeholders actually read detailed audit trails
- Math logs used for profit optimization and regulatory arbitrage
- Detailed calculation transparency required for investment product structuring

### Financial Product Reality
- **Customer policies** = raw material for institutional investment products
- **Ledger data** = intelligence for risk repackaging and partner organization operations
- **Settlement model** = relationship-based, phone calls and personal networks
- **No direct integration** = human-controlled data composition for deal making

## Technical Implications

### Separate Artifact Required
- **thena-ledger-client:** Independent domain optimized for massive data processing
- **Dependencies:** Contract client for business rules and configuration
- **Isolation:** Tenant-level database partitioning for business logic segregation

### Performance Characteristics
- **Write-heavy:** Massive append-only entry creation
- **Analytics-optimized:** Portfolio-level aggregation queries
- **Time-series:** Historical performance analysis across decades
- **Audit-compliant:** Immutable trail for regulatory and business intelligence

### Integration Points
- **Inbound:** Contract command processing
- **Outbound:** Sys log for external orchestrators, human-readable reports for partner organization operations
- **Tenant isolation:** Complete business logic separation per legacy product line