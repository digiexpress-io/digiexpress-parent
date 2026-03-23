import { useFsNav } from '@dxs-ts/fs-api';
import { FsDirentButtonCreateProps } from './FsDirentButtonCreateProps';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentButtonCreateProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  return { isDarkMode };
};
