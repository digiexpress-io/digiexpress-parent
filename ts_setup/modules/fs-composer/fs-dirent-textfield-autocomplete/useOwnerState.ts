import { useFsTheme } from '../fs-theme';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (): OwnerState => {
  const { isDarkMode } = useFsTheme();
  return { isDarkMode };
};
