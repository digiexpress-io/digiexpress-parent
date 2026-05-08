import { useFsNav } from '@dxs-ts/fs-nav';
import { FsPanelHelpProps } from "./FsPanelHelpProps";



export interface OwnerState {
    isDarkMode: boolean;
}

export const useOwnerState = (_props: FsPanelHelpProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode });
}
