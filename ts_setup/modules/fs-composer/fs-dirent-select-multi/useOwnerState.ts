import { useFsNav } from '@dxs-ts/fs-api';
import { FsDirentSelectMultiProps } from './FsDirentSelectMultiProps';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentSelectMultiProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  return { isDarkMode };
};
