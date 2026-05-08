import { FsPanelReferencesProps } from './FsPanelReferencesProps';
import { useFsTheme } from '../fs-theme';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsPanelReferencesProps): OwnerState => {
  const { isDarkMode } = useFsTheme();

  return ({ isDarkMode});
}
