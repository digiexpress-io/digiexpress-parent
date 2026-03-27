import { FsDirent, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentLanguageCreateProps {
  parentFolder: FsDirent | undefined;
  pathToTopParent: string | undefined;
}

export interface FsDirentLanguageUpdateProps {
  direntId: string;
}

export interface FsDirentLanguageProps {
  tab: FsTab;
}
