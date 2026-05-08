import { FsDirentButtonCreateProps } from './FsDirentButtonCreateProps';
import { useFsTheme } from '../fs-theme';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentButtonCreateProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  return { isDarkMode };
};
