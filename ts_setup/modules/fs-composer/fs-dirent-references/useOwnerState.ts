import { Fs, ItemReferencesEntry, useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentReferencesProps } from './FsDirentReferencesProps';


export interface OwnerState {
  findReferencesToDirent: (dirent: Fs.DirentBase) => ItemReferencesEntry[];
}

export const useOwnerState = (_props: FsDirentReferencesProps): OwnerState => {
  const { findReferencesToDirent } = useFsDirent();

  return ({ findReferencesToDirent });
}