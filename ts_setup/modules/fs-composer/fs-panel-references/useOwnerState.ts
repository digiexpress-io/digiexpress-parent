import { FsPanelReferencesProps } from './FsPanelReferencesProps';
import { useFsNav } from '@dxs-ts/fs-nav';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsPanelReferencesProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode});
}
