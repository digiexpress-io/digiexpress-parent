import { useFsTheme } from '../fs-theme';
import { FsPanelHelpProps } from "./FsPanelHelpProps";



export interface OwnerState {
    isDarkMode: boolean;
}

export const useOwnerState = (_props: FsPanelHelpProps): OwnerState => {
  const { isDarkMode } = useFsTheme();

  return ({ isDarkMode });
}
