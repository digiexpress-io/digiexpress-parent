import { Fs, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentPhoneCreateProps {
  parentFolder: Fs.DirentBase | undefined;
  pathToTopParent: string | undefined;
}

export interface FsDirentPhoneUpdateProps {
  direntId: string;
}

export interface FsDirentPhoneProps {
  tab: FsTab;
}
