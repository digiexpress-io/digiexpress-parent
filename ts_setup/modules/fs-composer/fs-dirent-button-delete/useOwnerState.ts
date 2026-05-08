import { FsDirentButtonDeleteProps } from './FsDirentButtonDeleteProps';
import { useFsNav } from '@dxs-ts/fs-nav';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentButtonDeleteProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  
  return { isDarkMode };
};
