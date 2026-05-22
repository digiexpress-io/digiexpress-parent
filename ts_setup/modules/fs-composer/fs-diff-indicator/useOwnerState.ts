import { useFsTheme } from '../fs-theme';
import { FsDiffIndicatorProps } from './FsDiffIndicatorProps';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDiffIndicatorProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  return { isDarkMode };
};
