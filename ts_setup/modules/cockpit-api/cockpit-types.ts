export namespace CockpitApi { }


export declare namespace CockpitApi {

  export type CockpitConfigId = string;
  export type CommitId = string;
  export type CreatedCommitId = string;

  export interface CreateCockpitCommand {
    configName: string;
    configDescription: string;
  }

  export interface CreateCockpitTenantCommand {
    externalId: string;
    tenantType: CockpitTenantType;
    tenantDescription: string;
  }

  export interface CockpitActivityChangeActiveIdCommand {
    activeId: string | undefined;
  }

  export type CockpitTenantType = 'WRENCH' | 'STENCIL'

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
    cockpitConfigTenantType: CockpitTenantType;
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


  export interface CockpitConfig {
    id: string;
    commitId: string;
    createdCommitId: string;
    updatedTreeCommitId: string;

    externalId?: string | undefined;

    cockpitConfigName: string;
    cockpitConfigDesc: string;

    docType: 'CONFIG';
  }

  export interface CockpitContainer {
    config: CockpitConfig;
    tenants: CockpitConfigTenant[];
  }

  export interface CockpitSummary {
    id: string;
    name: string;
    description: string;

    src: CockpitContainer;
    activity?: CockpitActivity | undefined;
  }

  export interface CockpitActivity {
    availableTenants: {
      stencil: CockpitConfigTenant[],
      wrench: CockpitConfigTenant[],
    },
    activeCockpitId: string | undefined;
  }
}
