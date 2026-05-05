import { Fs, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentFolderCreateProps {
  parentFolder: Fs.DirentBase | undefined;
}

export interface FsDirentFolderUpdateProps {
  direntId: string;
}

export interface FsDirentFolderProps {
  tab: FsTab;
}
