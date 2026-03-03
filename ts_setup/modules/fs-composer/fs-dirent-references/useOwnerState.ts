import { FsNode, ItemReferencesEntry, useFs } from '@dxs-ts/fs-api';
import { FsDirentReferencesProps } from './FsDirentReferencesProps';


export interface OwnerState {
  isDarkMode: boolean;
  findReferencesToNode:(node: FsNode) => ItemReferencesEntry[];
}

export const useOwnerState = (_props: FsDirentReferencesProps): OwnerState => {
  const { findReferencesToNode, isDarkMode } = useFs();

  return ({ findReferencesToNode, isDarkMode });
}