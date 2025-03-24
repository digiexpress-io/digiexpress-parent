import { HdesApi } from '@/api-wrench';

export interface Release {
  id: string;
  body: {
    name: string;
    note?: string;
    created: string;
    data?: string;
  };
  branches: ReleaseBranch[];
}

export interface ReleaseBranch {
  id: string;
  branch: HdesApi.AstBranch;
}
