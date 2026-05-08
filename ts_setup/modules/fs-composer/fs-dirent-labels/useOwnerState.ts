import { FsDirentLabelsProps } from './FsDirentLabelsProps';
import { useFsTheme } from '../fs-theme';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentLabelsProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
 
  return ({ isDarkMode });
};
