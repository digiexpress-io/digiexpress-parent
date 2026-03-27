import { FsDirent, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentFolderCreateProps {
  parentFolder: FsDirent | undefined;
  pathToTopParent: string | undefined;
}

export interface FsDirentFolderUpdateProps {
  direntId: string;
}

export interface FsDirentFolderProps {
  tab: FsTab;
}
