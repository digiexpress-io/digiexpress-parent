import { Fs, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentTemplateCreateProps {
  parentFolder: Fs.DirentBase | undefined;
  pathToTopParent: string | undefined;
}

export interface FsDirentTemplateUpdateProps {
  direntId: string;
}

export interface FsDirentTemplateProps {
  tab: FsTab;
}
