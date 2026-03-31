import { useFsNav } from '@dxs-ts/fs-api';
import { FsPanelReferencesProps } from './FsPanelReferencesProps';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsPanelReferencesProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode});
}
