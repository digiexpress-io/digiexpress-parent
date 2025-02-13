export type Instant = string;


export interface SysConfigDeployment {
  id: string;
  created: Instant;
  updated: Instant;
  liveDate: Instant;
  disabled: boolean;
  hash: string;
  tenantId: string;
  transactions: SysConfigDeploymentTransaction;
}

export interface SysConfigDeploymentTransaction {
  id: string;
  commands: SysConfigDeploymentCommand[]; 
}

export type SysConfigDeploymentCommandType = 'CreateDeployment' | 'UpdateDeploymentLiveDate' | 'UpdateDeploymentDisabled';

export interface SysConfigDeploymentCommand {
  commandType: SysConfigDeploymentCommandType;
}  

  
export interface CreateSysConfigDeployment extends SysConfigDeploymentCommand {
  deploymentId: string; // user given unique id
  disabled: boolean | undefined;
  liveDate: Instant;
  releaseId: string;
  commandType: 'CreateDeployment'
}


export interface SysConfigDeploymentUpdateCommand extends SysConfigDeploymentCommand {
  id: string;
}

export interface UpdateSysConfigDeploymentLiveDate extends SysConfigDeploymentUpdateCommand {
  liveDate: Instant;
  commandType: 'UpdateDeploymentLiveDate';
}


export interface UpdateSysConfigDeploymentDisabled extends SysConfigDeploymentUpdateCommand {
  disabled: boolean;
  commandType: 'UpdateDeploymentDisabled';
}

