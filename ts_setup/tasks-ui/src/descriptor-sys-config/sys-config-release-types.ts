import { SysConfigService } from './sys-config-types';


type Instant = string;

export interface SysConfigRelease extends Document {
  id: string;
  name: string;
  created: Instant;
  scheduledAt: Instant;
  author: string;
  tenantId: string;
  
  assets: SysConfigAsset[];
  services: SysConfigService[];  
}


export interface SysConfigAsset {
  id: string;
  name: string;
  version: string;
  updated: Instant
  
  body: string;
  bodyType: AssetType;
}

export type AssetType = 'DIALOB' | 'WRENCH' | 'STENCIL';


