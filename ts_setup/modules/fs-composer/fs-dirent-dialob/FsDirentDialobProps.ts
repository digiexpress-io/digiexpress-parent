import { FsDirent, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentDialobCreateProps {
  parentFolder: FsDirent.Dirent | undefined;
  pathToTopParent: string | undefined;
}

export interface FsDirentDialobUpdateProps {
  direntId: string;
}

export interface FsDirentDialobProps {
  tab: FsTab;
}
