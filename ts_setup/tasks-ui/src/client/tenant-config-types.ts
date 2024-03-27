export type TenantConfigId = string;
export type TenantConfigVersion = string;
export type TenantConfigName = string

export type TenantConfigStatus = 'IN_FORCE' | 'ARCHIVED';
export type TenantConfigTransactionId = string;

export type RepoId = string;
export type RepoType = (
  'PERMISSIONS' |
  'TASKS' |
  'CRM' |
  'STENCIL' |
  'WRENCH' |
  'DIALOB' |
  'CONFIG' |
  'HEALTH' |
  'USER_PROFILE' |
  'EXT_DIALOB' |
  'EXT_DIALOB_EDIT' |
  'TENANT' |
  'SYS_CONFIG');
export type AppType = 'APP_FRONTOFFICE' | "APP_CRM" | "APP_DIALOB" | "APP_STENCIL" | "APP_WRENCH" | "APP_TASKS";

export type RepoConfigType = Omit<RepoType, 'CONFIG' | 'HEALTH' | 'EXT_DIALOB' | 'TENANT' | 'EXT_DIALOB_EDIT'>;
export interface TenantConfigPreferences {
  landingApp: AppType | string
}

export interface RepoConfig {
  repoId: string;
  repoType: RepoConfigType;
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


