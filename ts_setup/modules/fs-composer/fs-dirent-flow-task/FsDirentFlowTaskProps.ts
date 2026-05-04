import { Fs, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentFlowTaskCreateProps {
  parentFolder: Fs.DirentBase | undefined;
  pathToTopParent: string | undefined;
}

export interface FsDirentFlowTaskUpdateProps {
  direntId: string;
}

export interface FsDirentFlowTaskProps {
  tab: FsTab;
}
