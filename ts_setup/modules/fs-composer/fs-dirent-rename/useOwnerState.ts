import { useFsNav } from '@dxs-ts/fs-api';
import { FsDirentRenameProps } from './FsDirentRenameProps';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentRenameProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode });
};
