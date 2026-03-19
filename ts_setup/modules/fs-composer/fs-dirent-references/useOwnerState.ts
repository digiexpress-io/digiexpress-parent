import { FsDirent, ItemReferencesEntry, useFs } from '@dxs-ts/fs-api';
import { FsDirentReferencesProps } from './FsDirentReferencesProps';


export interface OwnerState {
  isDarkMode: boolean;
  findReferencesToDirent: (dirent: FsDirent) => ItemReferencesEntry[];
}

export const useOwnerState = (_props: FsDirentReferencesProps): OwnerState => {
  const { findReferencesToDirent, isDarkMode } = useFs();

  return ({ findReferencesToDirent, isDarkMode });
}