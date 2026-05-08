import { Fs } from '@dxs-ts/fs-api';
import { FsTab } from '@dxs-ts/fs-nav';


export interface FsDirentFolderCreateProps {
  parentFolder: Fs.DirentBase | undefined;
}

export interface FsDirentFolderUpdateProps {
  direntId: string;
}

export interface FsDirentFolderProps {
  tab: FsTab;
}
