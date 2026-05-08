import { FsDirentHistoryProps } from './FsDirentHistoryProps';
import { useFsTheme } from '../fs-theme';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentHistoryProps): OwnerState => {
  const { isDarkMode } = useFsTheme();

  return ({ isDarkMode });
}