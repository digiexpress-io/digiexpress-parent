

export interface AssetRelease {
  id: number;
  body: {
    name: string;
    description: string;
    created?: Date;
    createdBy?: string;
    contentTag: string;
    wrenchTag: string;
    workflowTag: string;
  }
}

export interface AssetReleaseInit {
  id: number;
  body: {
    name: string;
    description: string | null;
    contentTag: string | null;
    wrenchTag: string | null;
    workflowTag: string | null;
  }
}