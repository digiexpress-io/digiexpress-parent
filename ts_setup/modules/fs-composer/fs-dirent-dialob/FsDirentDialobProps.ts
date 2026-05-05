import { Fs, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentDialobCreateProps {
  parentFolder: Fs.DirentBase | undefined;
}

export interface FsDirentDialobUpdateProps {
  direntId: string;
}

export interface FsDirentDialobProps {
  tab: FsTab;
}
