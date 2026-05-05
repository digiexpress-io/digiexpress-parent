import { Fs, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentLanguageCreateProps {
  parentFolder: Fs.DirentBase | undefined;
}

export interface FsDirentLanguageUpdateProps {
  direntId: string;
}

export interface FsDirentLanguageProps {
  tab: FsTab;
}
