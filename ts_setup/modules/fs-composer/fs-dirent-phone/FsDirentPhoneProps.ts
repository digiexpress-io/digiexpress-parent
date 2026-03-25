import { FsDirent, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentPhoneCreateProps {
  parentFolder: FsDirent | undefined;
  pathToTopParent: string | undefined;
}

export interface FsDirentPhoneUpdateProps {
  direntId: string;
}

export interface FsDirentPhoneProps {
  tab: FsTab;
}
