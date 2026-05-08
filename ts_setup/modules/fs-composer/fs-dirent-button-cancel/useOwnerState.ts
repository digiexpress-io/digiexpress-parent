import { FsDirentButtonCancelProps } from './FsDirentButtonCancelProps';
import { useFsNav } from '@dxs-ts/fs-nav';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentButtonCancelProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  return { isDarkMode };
};
