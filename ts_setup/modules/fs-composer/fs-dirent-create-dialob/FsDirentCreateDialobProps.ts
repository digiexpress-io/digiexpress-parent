import { FsDirent } from '@dxs-ts/fs-api';

export interface FsDirentCreateDialobProps {
  parentFolder: FsDirent | undefined;
  pathToTopParent: string | undefined;
}

