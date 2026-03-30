import { useFsNav } from '@dxs-ts/fs-api';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (): OwnerState => {
  const { isDarkMode } = useFsNav();
  return { isDarkMode };
};
