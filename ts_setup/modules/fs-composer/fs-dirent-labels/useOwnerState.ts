import { useFsNav } from '@dxs-ts/fs-api';
import { FsDirentLabelsProps } from './FsDirentLabelsProps';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentLabelsProps): OwnerState => {
  const { isDarkMode } = useFsNav();
 
  return ({ isDarkMode });
};
