import { FsDirentMenuNewProps } from './FsDirentMenuNewProps';
import { useFsTheme } from '../fs-theme';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentMenuNewProps): OwnerState => {
  const { isDarkMode } = useFsTheme();

  return ({ isDarkMode });
};
