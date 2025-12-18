export namespace LedgerApi {

}

export declare namespace LedgerApi {

  // ============================================================
  // Semantic ID Types
  // ============================================================
  export type LedgerId = string;
  export type MoneyRequestId = string;
  export type PaymentId = string;
  export type SettlementId = string;
  export type SettlementPaymentId = string;
  export type BlackBookId = string;
  export type BlackBookDetailId = string;
  export type ProjectionId = string;
  export type ProjectionDetailId = string;
  export type UnitPriceId = string;
  export type LedgerEventId = string;
  export type CommitId = string;

  // ============================================================
  // Literal union types (matching Java enums)
  // ============================================================
  export type LedgerDocType =
    | "LEDGER"
    | "MONEY_REQUEST"
    | "PAYMENT"
    | "SETTLEMENT"
    | "SETTLEMENT_PAYMENT"
    | "BLACK_BOOK"
    | "BLACK_BOOK_DETAIL"
    | "PROJECTION"
    | "PROJECTION_DETAIL"
    | "UNIT_PRICE"
    | "LEDGER_EVENT"
    | "COMMIT"
    | "COMMIT_TREE";

  export type LedgerRelationType =
    | "LEDGER"
    | "MONEY_REQUEST"
    | "PAYMENT"
    | "SETTLEMENT"
    | "BLACK_BOOK"
    | "PROJECTION";

  export type MoneyRequestStatus =
    | "OPEN"
    | "CLOSED"
    | "CANCELLED";

  export type MoneyRequestType =
    | "ADD_PAYMENT";

  // ============================================================
  // Transitive interfaces (for joins)
  // ============================================================
  export interface LedgerTransitives {
    createdAt: string; // OffsetDateTime
    updatedAt: string; // OffsetDateTime
    updatedTreeAt: string; // OffsetDateTime
  }

  export interface MoneyRequestTransitives {
    createdAt: string; // OffsetDateTime
    updatedAt: string; // OffsetDateTime
  }

  export interface PaymentTransitives {
    createdAt: string; // OffsetDateTime
  }

  export interface SettlementTransitives {
    createdAt: string; // OffsetDateTime
  }

  export interface SettlementPaymentTransitives {
    createdAt: string; // OffsetDateTime
  }

  export interface BlackBookTransitives {
    createdAt: string; // OffsetDateTime
  }

  export interface BlackBookDetailTransitives {
    createdAt: string; // OffsetDateTime
  }

  export interface ProjectionTransitives {
    createdAt: string; // OffsetDateTime
  }

  export interface ProjectionDetailTransitives {
    createdAt: string; // OffsetDateTime
  }

  export interface UnitPriceTransitives {
    createdAt: string; // OffsetDateTime
  }

  export interface LedgerEventTransitives {
    createdAt: string; // OffsetDateTime
  }

  // ============================================================
  // Ledger one-of relations (for polymorphic relations)
  // ============================================================
  export interface LedgerOneOfRelations {
    ledgerId?: LedgerId;
    moneyRequestId?: MoneyRequestId;
    paymentId?: PaymentId;
    settlementId?: SettlementId;
    blackBookId?: BlackBookId;
    projectionId?: ProjectionId;
    relationType: LedgerRelationType;
  }

  // ============================================================
  // Core Entity Interfaces (matching Java entities exactly)
  // ============================================================

  export interface Ledger {
    id: LedgerId;
    externalId: string;
    name: string;
    description?: string;
    currentBlackBookId?: BlackBookId;
    commitId: CommitId;
    createdCommitId: CommitId;
    updatedTreeCommitId: CommitId;

    // Transitive data from joins
    transitives?: LedgerTransitives;
  }

  export interface MoneyRequest {
    id: MoneyRequestId;
    ledgerId: LedgerId;
    externalId?: string;
    paymentId?: PaymentId;
    requestType: MoneyRequestType;
    requestStatus: MoneyRequestStatus;
    requestSubType?: string;
    requestDescription?: string;
    requestTargetDate: string; // LocalDate
    requestAmount: number; // BigDecimal
    commitId: CommitId;
    createdCommitId: CommitId;

    // Transitive data from joins
    transitives?: MoneyRequestTransitives;
  }

  export interface Payment {
    id: PaymentId;
    ledgerId: LedgerId;
    externalId: string;
    paymentType: string;
    paymentBody?: Record<string, any>; // JsonObject
    paymentSubType?: string;
    paymentDescription?: string;
    paymentDate: string; // LocalDate
    paymentAmount: number; // BigDecimal
    createdCommitId: CommitId;

    // Transitive data from joins
    transitives?: PaymentTransitives;
  }

  export interface Settlement {
    id: SettlementId;
    ledgerId: LedgerId;
    externalId: string;
    settlementType: string;
    settlementSubType?: string;
    settlementDescription?: string;
    settlementDate: string; // LocalDate
    settlementAmount: number; // BigDecimal
    createdCommitId: CommitId;

    // Transitive data from joins
    transitives?: SettlementTransitives;
  }

  export interface SettlementPayment {
    id: SettlementPaymentId;
    settlementId: SettlementId;
    paymentId: PaymentId;
    allocationAmount: number; // BigDecimal
    createdCommitId: CommitId;

    // Transitive data from joins
    transitives?: SettlementPaymentTransitives;
  }

  export interface BlackBook {
    id: BlackBookId;
    ledgerId: LedgerId;
    externalId?: string;
    parentBlackBookId?: BlackBookId;
    bookType: string;
    bookSubType?: string;
    bookDescription?: string;
    bookDate: string; // LocalDate
    bookAmount: number; // BigDecimal
    bookDeltaAmount?: number; // BigDecimal
    bookInflowAmount?: number; // BigDecimal
    bookOutflowAmount?: number; // BigDecimal
    createdCommitId: CommitId;

    // Transitive data from joins
    transitives?: BlackBookTransitives;
  }

  export interface BlackBookDetail {
    id: BlackBookDetailId;
    blackBookId: BlackBookId;
    detailType: string;
    detailAmount: number; // BigDecimal
    detailDeltaAmount?: number; // BigDecimal
    detailInflowAmount?: number; // BigDecimal
    detailOutflowAmount?: number; // BigDecimal
    externalId?: string;
    paymentId?: PaymentId;
    targetId?: string;
    detailSubType?: string;
    detailDescription?: string;
    detailStartDate?: string; // LocalDate
    detailEndDate?: string; // LocalDate
    detailFormula?: string;
    detailBody?: Record<string, any>; // JsonObject
    createdCommitId: CommitId;

    // Transitive data from joins
    transitives?: BlackBookDetailTransitives;
  }

  export interface Projection {
    id: ProjectionId;
    ledgerId: LedgerId;
    externalId: string;
    projectionType: string;
    projectionSubType?: string;
    projectionDescription?: string;
    projectionTargetDate: string; // LocalDate
    projectionStartDate: string; // LocalDate
    projectionEndDate: string; // LocalDate
    projectionAmount: number; // BigDecimal
    createdCommitId: CommitId;

    // Transitive data from joins
    transitives?: ProjectionTransitives;
  }

  export interface ProjectionDetail {
    id: ProjectionDetailId;
    projectionId: ProjectionId;
    externalId: string;
    detailType: string;
    detailSubType?: string;
    detailDescription?: string;
    targetId?: string;
    detailStartDate: string; // LocalDate
    detailEndDate: string; // LocalDate
    detailAmount: number; // BigDecimal
    detailFormula?: string;
    detailBody?: Record<string, any>; // JsonObject
    createdCommitId: CommitId;

    // Transitive data from joins
    transitives?: ProjectionDetailTransitives;
  }

  export interface UnitPrice {
    id: UnitPriceId;
    externalId: string;
    fundId: string;
    unitType: string;
    unitSubType?: string;
    unitDescription?: string;
    unitDate: string; // LocalDate
    unitValue: number; // BigDecimal
    createdCommitId: CommitId;

    // Transitive data from joins
    transitives?: UnitPriceTransitives;
  }

  export interface LedgerEvent {
    id: LedgerEventId;
    ledgerId: LedgerId;
    externalId: string;
    eventType: string;
    eventSubType?: string;
    eventDescription?: string;
    eventDate: string; // LocalDate
    body?: Record<string, any>; // JsonObject
    createdCommitId: CommitId;

    // Transitive data from joins
    transitives?: LedgerEventTransitives;
  }

  // ============================================================
  // Container interface (for grouped ledger data)
  // ============================================================
  export interface LedgerContainer {
    ledger: Ledger;
    ledgerEvents: LedgerEvent[];
    moneyRequests: MoneyRequest[];
    payments: Payment[];
    unitPrices: UnitPrice[];
    blackBooks: BlackBook[];
    projections: Projection[];
    settlements: Settlement[];

    // Map<BlackBookId, BlackBookDetail[]>
    blackBookDetails: Record<BlackBookId, BlackBookDetail[]>;

    // Map<ProjectionId, ProjectionDetail[]>
    projectionDetails: Record<ProjectionId, ProjectionDetail[]>;

    // Map<SettlementId, SettlementPayment[]>
    settlementPayments: Record<SettlementId, SettlementPayment[]>;
  }

  export interface LedgerSummary {
    ledgerId: string,
    contractNumber: string
    createdAt: Date,
    updatedAt: Date,
    currentBlackBook: BlackBook | undefined;
  }
}