import { Fs, ItemReferencesEntry, useFsDirent } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { FsDirentReferencesProps } from './FsDirentReferencesProps';


export interface OwnerState {
  isDarkMode: boolean;
  findReferencesToDirent: (dirent: Fs.DirentBase) => ItemReferencesEntry[];
}

export const useOwnerState = (_props: FsDirentReferencesProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const { findReferencesToDirent } = useFsDirent();

  return ({ findReferencesToDirent, isDarkMode });
}