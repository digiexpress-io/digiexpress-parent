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
    tenantDescription: string;
  }

  export interface CockpitActivityChangeActiveIdCommand {
    activeId: string | undefined;
  }
  export interface CockpitSummary {
    alias: {
      id: string;
      aliasName: string;
      aliasDesc: string;
      aliasTenantId?: string;
      refTenantId: string;
    },
    member?: {
      id: string;
      externalId: string;
      aliasId: string;
      aliasStatus: boolean;
    }
  }
}

