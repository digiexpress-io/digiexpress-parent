import { FsDirentRenameProps } from './FsDirentRenameProps';
import { useFsTheme } from '../fs-theme';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentRenameProps): OwnerState => {
  const { isDarkMode } = useFsTheme();

  return ({ isDarkMode });
};
