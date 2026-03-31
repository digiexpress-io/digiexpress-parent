import { FsDirent, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentFlowCreateProps {
  parentFolder: FsDirent.Dirent | undefined;
  pathToTopParent: string | undefined;
}

export interface FsDirentFlowUpdateProps {
  direntId: string;
}

export interface FsDirentFlowProps {
  tab: FsTab;
}
