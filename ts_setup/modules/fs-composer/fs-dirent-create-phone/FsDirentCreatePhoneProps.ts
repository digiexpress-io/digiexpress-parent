import { FsDirent } from '@dxs-ts/fs-api';

export interface FsDirentCreatePhoneProps {
  parentFolder: FsDirent | undefined;
  pathToTopParent: string | undefined;
}
