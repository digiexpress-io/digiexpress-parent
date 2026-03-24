import { FsDirent } from '@dxs-ts/fs-api';

export interface FsDirentCreateLanguageProps {
  parentFolder: FsDirent | undefined;
  pathToTopParent: string | undefined;
}

