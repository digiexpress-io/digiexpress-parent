import { Fs, ItemReferencesEntry, useFsDirent } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
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