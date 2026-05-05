import { Fs, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentPageCreateProps {
  parentFolder: Fs.DirentBase | undefined;
}

export interface FsDirentPageUpdateProps {
  direntId: string;
}

export interface FsDirentPageProps {
  tab: FsTab;
}
