import { FsDirentButtonCreateProps } from './FsDirentButtonCreateProps';
import { useFsNav } from '@dxs-ts/fs-nav';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentButtonCreateProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  return { isDarkMode };
};
