import { FsDirentButtonSaveProps } from './FsDirentButtonSaveProps';
import { useFsTheme } from '../fs-theme';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentButtonSaveProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  return { isDarkMode };
};
