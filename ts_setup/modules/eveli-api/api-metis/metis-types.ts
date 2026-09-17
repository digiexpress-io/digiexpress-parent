
export namespace MetisApi {

}

export declare namespace MetisApi {

  export type CapabilityState = 'READY' | 'NOT_READY' | 'DISABLED' | 'ERROR';

  export type JobState = 'NONE' | 'RUNNING' | 'CANCELLING' | 'CANCELLED' | 'COMPLETED' | 'FAILED';

  export interface CapabilityStatus {
    id: string;
    enabled: boolean;
    state: CapabilityState;
    detail?: string;
  }

  export interface Status {
    enabled: boolean;
    provider: string;
    chatModel: string;
    embeddingModel: string;
    capabilities: CapabilityStatus[];
  }

  export interface SearchIndexStatus {
    accepted: boolean;
    jobId?: number;
    state: JobState;
    totalCount?: number;
    processedCount: number;
    skippedCount: number;
    startedAt?: string;
    finishedAt?: string;
    durationMs?: number;
    error?: string;
    embeddingModel?: string;
    publicationId?: string;
    bundleHash?: string;
    indexedDocuments: number;
  }

  export interface ReindexCommand {
    force?: boolean;
    replace?: boolean;
  }
}
