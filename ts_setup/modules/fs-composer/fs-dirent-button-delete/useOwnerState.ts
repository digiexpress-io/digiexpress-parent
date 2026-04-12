import { useFsNav } from '@dxs-ts/fs-api';
import { FsDirentButtonDeleteProps } from './FsDirentButtonDeleteProps';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentButtonDeleteProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  
  return { isDarkMode };
};
