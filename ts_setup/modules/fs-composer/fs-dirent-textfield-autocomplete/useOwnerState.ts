import { useFsNav } from '@dxs-ts/fs-nav';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (): OwnerState => {
  const { isDarkMode } = useFsNav();
  return { isDarkMode };
};
