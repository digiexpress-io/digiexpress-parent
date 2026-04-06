import { Fs, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentServiceCreateProps {
  parentFolder: Fs.DirentBase | undefined;
  pathToTopParent: string | undefined;
}

export interface FsDirentServiceUpdateProps {
  direntId: string;
}

export interface FsDirentServiceProps {
  tab: FsTab;
}
