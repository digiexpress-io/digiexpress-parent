import { FsDirent, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentServiceCreateProps {
  parentFolder: FsDirent.Dirent | undefined;
  pathToTopParent: string | undefined;
}

export interface FsDirentServiceUpdateProps {
  direntId: string;
}

export interface FsDirentServiceProps {
  tab: FsTab;
}
