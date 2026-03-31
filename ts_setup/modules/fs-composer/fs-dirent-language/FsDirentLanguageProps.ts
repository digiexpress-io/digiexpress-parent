import { Fs, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentLanguageCreateProps {
  parentFolder: Fs.Dirent | undefined;
  pathToTopParent: string | undefined;
}

export interface FsDirentLanguageUpdateProps {
  direntId: string;
}

export interface FsDirentLanguageProps {
  tab: FsTab;
}
