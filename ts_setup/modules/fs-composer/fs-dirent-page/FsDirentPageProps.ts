import { Fs, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentPageCreateProps {
  parentFolder: Fs.DirentBase | undefined;
  pathToTopParent: string | undefined;
}

export interface FsDirentPageUpdateProps {
  direntId: string;
}

export interface FsDirentPageProps {
  tab: FsTab;
}
