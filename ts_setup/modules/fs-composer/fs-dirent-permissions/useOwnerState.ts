import { FsDirentPermissionsProps } from './FsDirentPermissionsProps';
import { useFsTheme } from '../fs-theme';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentPermissionsProps): OwnerState => {
  const { isDarkMode } = useFsTheme();

  return ({ isDarkMode });
}