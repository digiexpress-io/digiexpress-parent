import { FsDirentSelectMultiProps } from './FsDirentSelectMultiProps';
import { useFsTheme } from '../fs-theme';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentSelectMultiProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  return { isDarkMode };
};
