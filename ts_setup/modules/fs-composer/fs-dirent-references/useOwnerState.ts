import { FsDirent, ItemReferencesEntry, useFsNav, useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentReferencesProps } from './FsDirentReferencesProps';


export interface OwnerState {
  isDarkMode: boolean;
  findReferencesToDirent: (dirent: FsDirent) => ItemReferencesEntry[];
}

export const useOwnerState = (_props: FsDirentReferencesProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const { findReferencesToDirent } = useFsDirent();

  return ({ findReferencesToDirent, isDarkMode });
}