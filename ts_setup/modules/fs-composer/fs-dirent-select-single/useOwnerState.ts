import { useFsNav } from '@dxs-ts/fs-api';
import { FsDirentSelectSingleProps } from './FsDirentSelectSingleProps';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentSelectSingleProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  return { isDarkMode };
};
