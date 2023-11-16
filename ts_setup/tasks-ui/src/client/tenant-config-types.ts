export type TenantConfigId = string;
export type TenantConfigVersion = string;
export type TenantConfigName = string

export type TenantConfigStatus = 'IN_FORCE' | 'ARCHIVED';
export type TenantConfigTransactionId = string;

export type RepoId = string;
export type RepoName = 'TASKS' | 'CRM' | 'STENCIL' | 'WRENCH' | 'DIALOB';
export type AppType = 'APP_FRONTOFFICE' | "APP_CRM" | "APP_DIALOB" | "APP_STENCIL" | "APP_WRENCH" | "APP_TASKS";

export interface TenantConfigPreferences {
  landingApp: AppType
}

export interface RepoConfig {
  repoId: string,
  repoType: string
}

export interface TenantConfigTransaction {
  id: TenantConfigTransactionId,
  commands: TenantConfigCommandType[]
}

export interface TenantConfig {
  id: TenantConfigId,
  version: TenantConfigVersion | undefined,
  name: TenantConfigName,
  created: Date,
  updated: Date,
  archived: Date | undefined,
  status: TenantConfigStatus,
  preferences: TenantConfigPreferences,
  repoConfigs: RepoConfig[],
  transactions: TenantConfigTransaction[],
  documentType: "TENANT_CONFIG"
}

export type TenantConfigCommandType = 'ArchiveTenantConfig' | 'ChangeTenantConfigInfo' | 'CreateTenantConfig';

export interface TenantConfigCommand {
  userId: string,
  targetDate: Date,
  commandType: TenantConfigCommandType
}

export interface CreateTenantConfig extends TenantConfigCommand {
  repoId: RepoId,
  name: TenantConfigName
}

export interface TenantConfigUpdateCommand<T extends TenantConfigCommandType> extends TenantConfigCommand {
  tenantConfigId: TenantConfigId,
  commandType: T
}

export interface ChangeTenantConfigInfo extends TenantConfigUpdateCommand<'ChangeTenantConfigInfo'> {
  name: TenantConfigName
}

export interface ArchiveTenantConfig extends TenantConfigUpdateCommand<'ArchiveTenantConfig'> {
}


