import { FsDirent } from '@dxs-ts/fs-api';

export interface FsDirentCreateLinkProps {
  parentFolder: FsDirent | undefined;
  pathToTopParent: string | undefined;
}
