import { Fs, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentFlowCreateProps {
  parentFolder: Fs.DirentBase | undefined;
  pathToTopParent: string | undefined;
}

export interface FsDirentFlowUpdateProps {
  direntId: string;
}

export interface FsDirentFlowProps {
  tab: FsTab;
}
