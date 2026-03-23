import { useFsNav } from '@dxs-ts/fs-api';
import { FsDirentMenuNewProps } from './FsDirentMenuNewProps';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentMenuNewProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode });
};
