import { Fs, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentPrintoutCreateProps {
  parentFolder: Fs.DirentBase | undefined;
  pathToTopParent: string | undefined;
}

export interface FsDirentPrintoutUpdateProps {
  direntId: string;
  pathToTopParent: string | undefined;
}

export interface FsDirentPrintoutProps {
  tab: FsTab;
}
