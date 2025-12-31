export namespace CockpitApi { }


export declare namespace CockpitApi {

  export type CockpitConfigId = string;
  export type CommitId = string;
  export type CreatedCommitId = string;

  export type CockpitDocType =
    | "CONFIG"
    | "CONFIG_TENANT"
    | "CONFIG_PROPS"
    | "CONFIG_COMMIT"
    | "CONFIG_COMMIT_TREE";


  export interface CockpitConfigTenant {
    id: string;
    cockpitConfigId: CockpitConfigId;
    commitId: CommitId;
    createdCommitId: CreatedCommitId;

    externalId: string;
    externalBranch: string;

    cockpitConfigTenantDesc: string;
    cockpitConfigTenantExtension?: Object | undefined;

    docType: 'CONFIG_TENANT'
  }

  export interface CockpitConfigProps {
    id: string;
    cockpitConfigId: CockpitConfigId;
    commitId: CommitId;
    createdCommitId: CreatedCommitId;

    cockpitConfigPropsType: string;
    getCockpitConfigPropsExtension?: Object | undefined;

    docType: 'CONFIG_PROPS'
  }

  export interface CockpitEntity {
    id: string;
    commitId: CommitId;
    createdCommitId: CreatedCommitId;
    updatedTreeCommitId: string;

    externalId?: string | undefined;
    cockpitConfigName: string;
    cockpitConfigDesc: string;

    docType: 'CONFIG'
  }

  export interface CockpitContainer {

  }

  export interface CockpitSummary {
    container: CockpitContainer;
    cockpitConfigId: CockpitConfigId;
    contractNumber: string;
    contractIssueDate: Date;
    contractStartDate: Date;
    contractMaturityDate?: Date;
    contractStatusIntl?: string;

    contractType: string;

    createdAt: Date;
    updatedAt: Date;

  }

}
