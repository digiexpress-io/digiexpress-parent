import { FsDirentButtonCancelProps } from './FsDirentButtonCancelProps';
import { useFsTheme } from '../fs-theme';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentButtonCancelProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  return { isDarkMode };
};
