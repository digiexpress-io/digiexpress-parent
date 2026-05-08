import { FsDirentSelectSingleProps } from './FsDirentSelectSingleProps';
import { useFsNav } from '@dxs-ts/fs-nav';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentSelectSingleProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  return { isDarkMode };
};
