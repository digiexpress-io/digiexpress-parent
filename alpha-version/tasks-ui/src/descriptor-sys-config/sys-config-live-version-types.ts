type Instant = string;

export interface SysConfigLiveVersion extends Document {
  id: string;

  updated: Instant;
  lastCheck: Instant;
  releaseHash: string;
  releaseCreated: string;
  releaseId: string;
  releaseName: string;
}
