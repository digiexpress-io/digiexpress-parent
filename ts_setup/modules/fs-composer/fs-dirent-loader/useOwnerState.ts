import { FsDirentLoaderProps } from './FsDirentLoaderProps';
import { useFsTheme } from '../fs-theme';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentLoaderProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  return { isDarkMode };
};
