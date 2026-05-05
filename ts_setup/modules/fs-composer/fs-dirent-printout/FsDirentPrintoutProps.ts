import { Fs, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentPrintoutCreateProps {
  parentFolder: Fs.DirentBase | undefined;
}

export interface FsDirentPrintoutUpdateProps {
  direntId: string;
}

export interface FsDirentPrintoutProps {
  tab: FsTab;
}
