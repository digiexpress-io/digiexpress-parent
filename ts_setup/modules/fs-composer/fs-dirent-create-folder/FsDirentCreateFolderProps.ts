import { FsDirent } from '@dxs-ts/fs-api';

export interface FsDirentCreateFolderProps {
  parentFolder: FsDirent | undefined;
  pathToTopParent: string | undefined;
}

