import { Fs, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentArticleCreateProps {
  parentFolder: Fs.DirentBase | undefined;
  pathToTopParent: string | undefined;
}

export interface FsDirentArticleUpdateProps {
  direntId: string;
}

export interface FsDirentArticleProps {
  tab: FsTab;
}
