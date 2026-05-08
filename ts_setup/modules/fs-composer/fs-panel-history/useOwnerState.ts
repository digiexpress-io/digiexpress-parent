import { FsPanelHistoryProps } from './FsPanelHistoryProps';
import { useFsTheme } from '../fs-theme';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsPanelHistoryProps): OwnerState => {
  const { isDarkMode } = useFsTheme();

  return ({ isDarkMode});
}
