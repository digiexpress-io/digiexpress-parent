import { defineMessages } from "react-intl";

export namespace ContractApi {

  enum Colors {
    RED = 1,
    BLUE,
    GREEN,
    YELLOW,
    GREY
  }

  const color_hex: Record<Colors, string> = {
    [Colors.RED]:    '#f44336',
    [Colors.BLUE]:   '#2196f3',
    [Colors.GREEN]:  '#4caf50',
    [Colors.YELLOW]: '#ffeb3b',
    [Colors.GREY]:   '#9e9e9e',
  };

  type ColorMap = {
    [status: string]: Colors
  }
  

  export const contract_status_colors: ColorMap = {
    NEW: Colors.YELLOW,
    OPEN: Colors.BLUE,
    COMPLETED: Colors.GREEN,
    REJECTED: Colors.RED,
    TRANSFERRED: Colors.GREY,
    DELEGATED: Colors.GREY,
    WAITING: Colors.GREY
  };
  export const contract_status_hex = Object.fromEntries(
    Object.entries(contract_status_colors).map(([k, apiColor]) => [
      k, color_hex[apiColor as Colors]
    ])
  ) as Record<ContractStatusType, string>;

  export const contract_status_messages = defineMessages({
    'ACTIVE': {
      id: 'contract.status.active',
    },
  });
}

export declare namespace ContractApi {

  // ============================================================
  // Semantic ID Types
  // ============================================================
  export type ContractId = string;
  export type PartyId = string;
  export type CoverageId = string;
  export type CapabilityId = string;
  export type PaymentPlanId = string;
  export type InvPlanId = string;
  export type InvPlanAllocId = string;
  export type ReferenceId = string;
  export type NoteId = string;
  export type CommitId = string;
  export type CommitTreeId = string;
  export type ContractEntityId = string;
  export type ContractStatusType = 'ACTIVE';

  // ============================================================
  // Literal union types (matching Java enums)
  // ============================================================
  export type ContractDocType =
    | "CONTRACT"
    | "PARTY"
    | "COVERAGE"
    | "NOTE"
    | "CAPABILITY"
    | "PAYMENT_PLAN"
    | "INV_PLAN"
    | "INV_PLAN_ALLOC"
    | "REFERENCE"
    | "COMMIT"
    | "COMMIT_TREE";

  export type CommitTreeOperation =
    | "ADD"
    | "REMOVE"
    | "MERGE";

  export type ContractRelationType =
    | "INV_PLAN"
    | "INV_PLAN_ALLOC"
    | "COVERAGE"
    | "PARTY";

  // ============================================================
  // Transitive interfaces (for joins)
  // ============================================================
  export interface ContractTransitives {
    createdAt: string; // OffsetDateTime
    updatedAt: string; // OffsetDateTime
    updatedTreeAt: string; // OffsetDateTime
  }

  export interface PartyTransitives {
    createdAt: string; // OffsetDateTime
    updatedAt: string; // OffsetDateTime
  }

  export interface CoverageTransitives {
    createdAt: string; // OffsetDateTime
    updatedAt: string; // OffsetDateTime
  }

  export interface CapabilityTransitives {
    createdAt: string; // OffsetDateTime
    updatedAt: string; // OffsetDateTime
  }

  export interface PaymentPlanTransitives {
    createdAt: string; // OffsetDateTime
    updatedAt: string; // OffsetDateTime
  }

  export interface InvPlanTransitives {
    createdAt: string; // OffsetDateTime
    updatedAt: string; // OffsetDateTime
  }

  export interface InvPlanAllocTransitives {
    createdAt: string; // OffsetDateTime
    updatedAt: string; // OffsetDateTime
    contractId: ContractId; // Virtual field from parent table
  }

  export interface NoteTransitives {
    createdAt: string; // OffsetDateTime
    updatedAt: string; // OffsetDateTime
  }

  export interface ReferenceTransitives {
    createdAt: string; // OffsetDateTime
    updatedAt: string; // OffsetDateTime
  }

  // ============================================================
  // Contract one-of relations (for Note/Reference multi-FK)
  // ============================================================
  export interface ContractOneOfRelations {
    relationType: ContractRelationType;
    invPlanId?: InvPlanId;
    invPlanAllocId?: InvPlanAllocId;
    coverageId?: CoverageId;
    partyId?: PartyId;
  }

  // ============================================================
  // Core Entity Interfaces (matching Java entities exactly)
  // ============================================================

  export interface Contract {
    id: ContractId;
    parentContractId?: ContractId;
    contractNumber: string;

    externalId?: string;
    commitId: CommitId;
    createdCommitId: CommitId;
    updatedTreeCommitId: CommitId;

    // Transitive data from joins
    transitives?: ContractTransitives;

    // Business dates (expanded)
    contractIssueDate: string; // LocalDate
    contractIssueDateInterval: string; // Period -> ISO-8601 duration string
    contractIssueDateType: string;
    
    contractStartDate: string; // LocalDate
    contractStartDateInterval: string; // Period -> ISO-8601 duration string
    contractStartDateType: string;
    
    contractMaturityDate?: string; // Optional<LocalDate>
    contractMaturityDateInterval?: string; // Optional<Period>
    contractMaturityDateType?: string;
    
    contractStatus: ContractStatusType;
    contractSubStatus?: string;
    contractType: string;
    contractSubType?: string;
    contractData?: Record<string, any>; // JsonObject
  }

  export interface Party {
    id: PartyId;
    contractId: ContractId;

    externalId: string;
    commitId: CommitId;
    createdCommitId: CommitId;

    // Transitive data from joins
    transitives?: PartyTransitives;

    partyType: string;
    partyEffectiveFrom: string; // LocalDate
    partyEffectiveTo?: string; // Optional<LocalDate>

    // Business dates (expanded)
    partyTermStartDate: string; // LocalDate
    partyTermStartDateInterval: string; // Period
    partyTermStartDateType: string;

    partyTermEndDate?: string; // Optional<LocalDate>
    partyTermEndDateInterval?: string; // Optional<Period>
    partyTermEndDateType?: string;

    partyData?: Record<string, any>; // Optional<JsonObject>
  }

  export interface Coverage {
    id: CoverageId;
    contractId: ContractId;
    insuredId: PartyId; // Note: it's insuredId in Java, not insuredPartyId

    externalId: string;
    commitId: CommitId;
    createdCommitId: CommitId;

    // Transitive data from joins
    transitives?: CoverageTransitives;

    coverageType: string;
    coverageCode: string;
    coverageSumInsured?: number; // Optional<BigDecimal>
    coverageRate?: number; // Optional<BigDecimal>
    coverageRateType?: string;
    coverageStatus: string;
    coverageEffectiveFrom: string; // LocalDate
    coverageEffectiveTo?: string; // Optional<LocalDate>

    // Business dates (expanded)
    coverageTermStartDate: string; // LocalDate
    coverageTermStartDateInterval: string; // Period
    coverageTermStartDateType: string;

    coverageTermEndDate?: string; // Optional<LocalDate>
    coverageTermEndDateInterval?: string; // Optional<Period>
    coverageTermEndDateType?: string;
  }

  export interface Capability {
    id: CapabilityId;
    contractId: ContractId;

    externalId?: string;
    commitId: CommitId;
    createdCommitId: CommitId;

    // Transitive data from joins
    transitives?: CapabilityTransitives;

    capabilityCode: string;
    capabilityName: string;
    capabilityValue?: string;
  }

  export interface PaymentPlan {
    id: PaymentPlanId;
    contractId: ContractId;

    partyId?: PartyId; // Optional<String>
    commitId: CommitId;
    createdCommitId: CommitId;

    // Transitive data from joins
    transitives?: PaymentPlanTransitives;

    paymentPlanStatus: string;
    paymentPlanFrequency: string;
    paymentPlanAmount: number; // BigDecimal

    // Business dates (expanded)
    paymentPlanStartDate: string; // LocalDate
    paymentPlanStartDateInterval: string; // Period
    paymentPlanStartDateType: string;

    paymentPlanEndDate?: string; // Optional<LocalDate>
    paymentPlanEndDateInterval?: string; // Optional<Period>
    paymentPlanEndDateType?: string;
  }

  export interface InvPlan {
    id: InvPlanId;
    contractId: ContractId;

    externalId: string;
    commitId: CommitId;
    createdCommitId: CommitId;

    // Transitive data from joins
    transitives?: InvPlanTransitives;

    invPlanStatus: string;
    invPlanCode: string;
    invPlanName: string;

    // Business dates (expanded)
    invPlanStartDate: string; // LocalDate
    invPlanStartDateInterval: string; // Period
    invPlanStartDateType: string;

    invPlanEndDate?: string; // Optional<LocalDate>
    invPlanEndDateInterval?: string; // Optional<Period>
    invPlanEndDateType?: string;
  }

  export interface InvPlanAlloc {
    id: InvPlanAllocId;
    invPlanId: InvPlanId;

    commitId: CommitId;
    createdCommitId: CommitId;

    // Transitive data from joins (includes virtual contractId)
    transitives?: InvPlanAllocTransitives;

    invPlanAllocCode: string;
    invPlanAllocName: string;
    invPlanAllocPercentage: number; // BigDecimal
    invPlanAllocStatus: string;
  }

  export interface Note {
    id: NoteId;
    contractId: ContractId;

    // Multi-FK relations resolver
    relations?: ContractOneOfRelations;

    commitId: CommitId;
    createdCommitId: CommitId;

    // Transitive data from joins
    transitives?: NoteTransitives;

    noteCode: string;
    noteType: string;
    noteTitle: string;
    noteBody?: Record<string, any>; // Optional<JsonObject>
  }

  export interface Reference {
    id: ReferenceId;
    contractId: ContractId;

    // Multi-FK relations resolver
    relations?: ContractOneOfRelations;

    commitId: CommitId;
    createdCommitId: CommitId;

    // Transitive data from joins
    transitives?: ReferenceTransitives;

    referenceCode: string;
    referenceType: string;
    referenceTitle: string;
    referenceBody?: Record<string, any>; // Optional<JsonObject>
  }

  export interface Commit {
    commitId: CommitId;
    parentCommitId?: CommitId;
    contractId?: ContractId;
    createdAt: string; // OffsetDateTime
    commitLog: string;
    commitAuthor: string;
    commitMessage: string;
  }

  export interface CommitTree {
    id: CommitTreeId;
    commitId: CommitId;
    operationType: CommitTreeOperation;
    bodyAfter?: Record<string, any>;
    bodyBefore?: Record<string, any>;
  }

  // ============================================================
  // Container interface (for grouped contract data)
  // ============================================================
  export interface ContractContainer {
    contract: Contract;
    parties: Party[];
    coverages: Coverage[];
    references: Reference[];
    notes: Note[];
    capabilities: Capability[];
    invPlans: InvPlan[];
    paymentPlans: PaymentPlan[];

    // Map<InvPlanId, InvPlanAlloc[]>
    invPlanAllocations: Record<InvPlanId, InvPlanAlloc[]>;
  }
}