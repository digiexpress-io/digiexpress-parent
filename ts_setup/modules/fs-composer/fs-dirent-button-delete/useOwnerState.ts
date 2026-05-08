import { FsDirentButtonDeleteProps } from './FsDirentButtonDeleteProps';
import { useFsTheme } from '../fs-theme';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentButtonDeleteProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  
  return { isDarkMode };
};
