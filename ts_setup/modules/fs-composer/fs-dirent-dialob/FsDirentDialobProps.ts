import { Fs, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentDialobCreateProps {
  parentFolder: Fs.Dirent | undefined;
  pathToTopParent: string | undefined;
}

export interface FsDirentDialobUpdateProps {
  direntId: string;
}

export interface FsDirentDialobProps {
  tab: FsTab;
}
