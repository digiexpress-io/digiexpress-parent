import { FsDirentLabelsProps } from './FsDirentLabelsProps';
import { useFsNav } from '@dxs-ts/fs-nav';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentLabelsProps): OwnerState => {
  const { isDarkMode } = useFsNav();
 
  return ({ isDarkMode });
};
