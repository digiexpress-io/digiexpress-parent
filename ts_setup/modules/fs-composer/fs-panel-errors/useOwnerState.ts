import { FsPanelErrorsProps } from './FsPanelErrorsProps';
import { useFsTheme } from '../fs-theme';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsPanelErrorsProps): OwnerState => {
  const { isDarkMode } = useFsTheme();

  return ({ isDarkMode});
}