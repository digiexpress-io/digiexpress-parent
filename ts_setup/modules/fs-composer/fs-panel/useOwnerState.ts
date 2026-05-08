import { FsPanelProps } from './FsPanelProps';
import { useFsTheme } from '../fs-theme';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsPanelProps): OwnerState => {
  const { isDarkMode } = useFsTheme();

  return ({ isDarkMode});
}