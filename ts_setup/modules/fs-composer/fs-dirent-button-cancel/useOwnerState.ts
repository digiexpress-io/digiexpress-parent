import { useFsNav } from '@dxs-ts/fs-api';
import { FsDirentButtonCancelProps } from './FsDirentButtonCancelProps';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentButtonCancelProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  return { isDarkMode };
};
