import { Fs, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentLinkCreateProps {
  parentFolder: Fs.DirentBase | undefined;
}

export interface FsDirentLinkUpdateProps {
  direntId: string;
}

export interface FsDirentLinkProps {
  tab: FsTab;
}
