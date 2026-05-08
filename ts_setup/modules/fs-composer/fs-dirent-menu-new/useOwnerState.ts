import { FsDirentMenuNewProps } from './FsDirentMenuNewProps';
import { useFsNav } from '@dxs-ts/fs-nav';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentMenuNewProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode });
};
