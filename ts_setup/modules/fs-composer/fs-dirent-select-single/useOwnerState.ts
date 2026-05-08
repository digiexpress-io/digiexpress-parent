import { FsDirentSelectSingleProps } from './FsDirentSelectSingleProps';
import { useFsTheme } from '../fs-theme';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentSelectSingleProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  return { isDarkMode };
};
