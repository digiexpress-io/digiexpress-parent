import { useFsNav } from '@dxs-ts/fs-api';
import { FsDirentMultiSelectProps } from './FsDirentMultiSelectProps';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentMultiSelectProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  return { isDarkMode };
};
