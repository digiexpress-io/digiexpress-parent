import { FsDirent } from '@dxs-ts/fs-api';

export interface FsDirentCreatePrintoutProps {
  parentFolder: FsDirent | undefined;
  pathToTopParent: string | undefined;
}

