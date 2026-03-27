import { useFsNav } from '@dxs-ts/fs-api';
import { FsDirentSingleSelectProps } from './FsDirentSingleSelectProps';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentSingleSelectProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  return { isDarkMode };
};
