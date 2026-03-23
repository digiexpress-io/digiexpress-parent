import { FsDirent } from '@dxs-ts/fs-api';

export interface FsDirentCreateServiceProps {
  parentFolder: FsDirent | undefined;
  pathToTopParent: string | undefined;
}
