import { FsDirent, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentLinkCreateProps {
  parentFolder: FsDirent | undefined;
  pathToTopParent: string | undefined;
}

export interface FsDirentLinkUpdateProps {
  direntId: string;
}

export interface FsDirentLinkProps {
  tab: FsTab;
}
