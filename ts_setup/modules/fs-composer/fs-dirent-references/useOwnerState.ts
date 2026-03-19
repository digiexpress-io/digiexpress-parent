import { FsDirent, ItemReferencesEntry, useFsNav } from '@dxs-ts/fs-api';
import { FsDirentReferencesProps } from './FsDirentReferencesProps';


export interface OwnerState {
  isDarkMode: boolean;
  findReferencesToDirent: (dirent: FsDirent) => ItemReferencesEntry[];
}

export const useOwnerState = (_props: FsDirentReferencesProps): OwnerState => {
  const { findReferencesToDirent, isDarkMode } = useFsNav();

  return ({ findReferencesToDirent, isDarkMode });
}