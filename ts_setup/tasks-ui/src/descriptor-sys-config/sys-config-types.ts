type Instant = string;

export interface SysConfig {
  id: string;

  name: string;
  created: Instant;
  updated: Instant;
  
  tenantId: string;
  wrenchHead: string;
  stencilHead: string;  
  
  services: SysConfigService[];
  transactions: SysConfigTransaction[];
}

export interface SysConfigTransaction {
  id: string;
  commands: SysConfigCommand[];
}

export interface SysConfigService {
  id: string | undefined; //on creating 
  serviceName: string;
  formId: string;
  flowName: string;
  locales: string;
}

export type SysConfigCommandType = 'CreateSysConfig' | 'CreateSysConfigRelease';

export interface SysConfigCommand {
  userId: string;
  targetDate: Instant;
  commandType: SysConfigCommandType;
}

export interface CreateSysConfig extends SysConfigCommand {
  tenantId: string;
  name: string;
  wrenchHead: string;
  stencilHead: string; 
  services: SysConfigService[];
  commandType: 'CreateSysConfig';
}

export interface SysConfigUpdateCommand extends SysConfigCommand {
  id: string;
}

export interface CreateSysConfigRelease extends SysConfigUpdateCommand {
  releaseName: string;
  scheduledAt: Instant;
  commandType: 'CreateSysConfigRelease';
}

export interface SysConfigStore {
  findAllSysConfigs(): Promise<SysConfig[]>;
  getOneSysConfig(sysConfigId: string): Promise<SysConfig>;
  createOneSysConfig(commands: CreateSysConfig): Promise<SysConfig>;
  updateOneSysConfig(sysConfigId: string, commands: SysConfigUpdateCommand[]): Promise<SysConfig>;
  deleteOneSysConfig(sysConfigId: string, commands: SysConfigUpdateCommand[]): Promise<SysConfig>;
}