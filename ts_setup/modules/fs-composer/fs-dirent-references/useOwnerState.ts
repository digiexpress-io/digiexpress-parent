import { Fs, ItemReferencesEntry, useFsNav, useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentReferencesProps } from './FsDirentReferencesProps';


export interface OwnerState {
  isDarkMode: boolean;
  findReferencesToDirent: (dirent: Fs.DirentBase) => ItemReferencesEntry[];
}

export const useOwnerState = (_props: FsDirentReferencesProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const { findReferencesToDirent } = useFsDirent();

  return ({ findReferencesToDirent, isDarkMode });
}