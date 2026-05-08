import { FsDirentSelectMultiProps } from './FsDirentSelectMultiProps';
import { useFsNav } from '@dxs-ts/fs-nav';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentSelectMultiProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  return { isDarkMode };
};
