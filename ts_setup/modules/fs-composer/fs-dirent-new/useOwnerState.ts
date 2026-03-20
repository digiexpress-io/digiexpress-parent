import { useFsNav } from '@dxs-ts/fs-api';
import { FsDirentNewProps } from './FsDirentNewProps';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentNewProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode });
};
