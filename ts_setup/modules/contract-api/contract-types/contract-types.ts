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

  // ============================================================
  // Literal union types
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

  export interface Commit {
    commitId: CommitId;
    parentCommitId?: CommitId;
    contractId: ContractId;
    createdAt: string;
    commitLog: string;
    docType: ContractDocType;
  }

  export interface CommitTree {
    id: CommitTreeId;
    commitId: CommitId;
    operationType: CommitTreeOperation;
    bodyAfter?: Record<string, any>;
    bodyBefore?: Record<string, any>;
    docType: ContractDocType;
  }

  export interface Contract {
    id: ContractId;
    parentContractId?: ContractId;
    contractName: string;
    contractNumber: string;
    externalId?: string;
    commitId: CommitId;
    createdCommitId: CommitId;
    docType: ContractDocType;
  }

  export interface ContractEntity {
    id: ContractEntityId;
    docType: ContractDocType;
    relationType: string;
  }

  export interface Coverage {
    id: CoverageId;
    contractId: ContractId;
    insuredPartyId: PartyId;
    coverageType: string;
    externalId: string;

    commitId: CommitId;
    createdCommitId: CommitId;

    coverageEffectiveFrom: string;
    coverageEffectiveTo?: string;

    coverageTermStartDate: string;
    coverageTermStartDateInterval: string;
    coverageTermStartDateType: string;

    coverageTermEndDate?: string;
    coverageTermEndDateInterval?: string;
    coverageTermEndDateType?: string;

    docType: ContractDocType;
  }

  export interface Party {
    id: PartyId;
    contractId: ContractId;
    externalId: string;
    partyType: string;
    commitId: CommitId;
    createdCommitId: CommitId;
    docType: ContractDocType;
  }

  export interface Capability {
    id: CapabilityId;
    contractId: ContractId;
    externalId?: string;
    capabilityCode: string;
    capabilityValue?: string;
    commitId: CommitId;
    createdCommitId: CommitId;
    docType: ContractDocType;
  }

  export interface PaymentPlan {
    id: PaymentPlanId;
    contractId: ContractId;
    externalId?: string;
    planCode: string;
    planValue?: string;
    commitId: CommitId;
    createdCommitId: CommitId;
    docType: ContractDocType;
  }

  export interface Note {
    id: NoteId;
    contractId: ContractId;
    commitId: CommitId;
    createdCommitId: CommitId;
    noteType: string;
    noteValue: string;
    docType: ContractDocType;
  }

  export interface Reference {
    id: ReferenceId;
    contractId: ContractId;
    commitId: CommitId;
    createdCommitId: CommitId;
    referenceType: string;
    referenceValue: string;
    docType: ContractDocType;
  }

  export interface InvPlan {
    id: InvPlanId;
    contractId: ContractId;
    externalId?: string;
    invPlanCode: string;
    invPlanValue?: string;

    commitId: CommitId;
    createdCommitId: CommitId;
    docType: ContractDocType;

    createdAt: string;
    updatedAt: string;
  }

  export interface InvPlanAlloc {
    id: InvPlanAllocId;
    invPlanId: InvPlanId;
    externalId?: string;
    invPlanAllocCode: string;
    invPlanAllocValue?: string;

    commitId: CommitId;
    createdCommitId: CommitId;
    docType: ContractDocType;
  }

  export interface ContractContainer {
    contract: Contract;
    parties: Party[];
    coverages: Coverage[];
    references: Reference[];
    notes: Note[];
    capabilities: Capability[];
    invPlans: InvPlan[];
    paymentPlans: PaymentPlan[];

    // Map<String, List<InvPlanAlloc>>
    invPlanAllocations: Record<InvPlanId, InvPlanAlloc[]>;
  }
}
